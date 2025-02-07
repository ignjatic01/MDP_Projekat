package net.etf.service;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Properties;

import net.etf.model.User;

public class UserService 
{
	
	public static final String PATH;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(System.getProperty("user.dir") + File.separator + "src" + File.separator + "resources/config.properties")) {
            prop.load(input);
            PATH = prop.getProperty("MAIN_PATH");
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
	public UserService()
	{
		
	}
	
	public boolean login(String username, String pass)
	{
		ArrayList<User> users = deserializeWithXML();
		User temp = new User(username);
		for(User u: users)
		{
			System.out.println(u.getUsername());
			if(u.equals(temp))
			{
				if(u.isAlowed() && u.getPassword().equals(pass) && !u.isBlocked())
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public String getUserEmail(String username)
	{
		ArrayList<User> users = deserializeWithXML();
		User temp = new User(username);
		for(User u: users)
		{
			if(u.equals(temp))
			{
				return u.getEmail();
			}
		}
		return "";
	}
	
	public boolean register(User user)
	{
		ArrayList<User> users = deserializeWithXML();
		System.out.println(System.getProperty("user.dir"));
		User newUser = user;
		for(User u : users)
		{
			if(u.equals(newUser))
			{
				return false;
			}
		}
		users.add(newUser);
		serializeWithXML(users);
		return true;
	}
	
	public ArrayList<User> getNotAllowedUsers()
	{
		ArrayList<User> notAllowedUsers = new ArrayList<>();
		ArrayList<User> users = deserializeWithXML();
		for(User u : users)
		{
			if(!u.isAlowed())
			{
				notAllowedUsers.add(u);
			}
		}
		return notAllowedUsers;
	}
	
	public ArrayList<User> getAllowedUsers()
	{
		ArrayList<User> allowedUsers = new ArrayList<>();
		ArrayList<User> users = deserializeWithXML();
		for(User u : users)
		{
			if(u.isAlowed())
			{
				allowedUsers.add(u);
			}
		}
		return allowedUsers;
	}
	
	public boolean allowUser(String username)
	{
		ArrayList<User> users = deserializeWithXML();
		User temp = new User(username);
		User selectedUser = null;
		for(User u : users)
		{
			if(temp.equals(u))
			{
				selectedUser = u;
				break;
			}
		}
		if(selectedUser != null)
		{
			users.remove(temp);
			selectedUser.setAlowed(true);
			users.add(selectedUser);
			serializeWithXML(users);
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	public boolean blockUser(String username, boolean blocking)
	{
		ArrayList<User> users = deserializeWithXML();
		User temp = new User(username);
		User selectedUser = null;
		for(User u : users)
		{
			if(temp.equals(u))
			{
				selectedUser = u;
				break;
			}
		}
		if(selectedUser != null)
		{
			users.remove(temp);
			selectedUser.setBlocked(blocking);
			users.add(selectedUser);
			serializeWithXML(users);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean deleteUser(String username)
	{
		ArrayList<User> users = deserializeWithXML();
		User temp = new User(username);
		User selectedUser = null;
		for(User u : users)
		{
			if(temp.equals(u))
			{
				selectedUser = u;
				break;
			}
		}
		if(selectedUser != null)
		{
			users.remove(temp);
			serializeWithXML(users);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void serializeWithXML(ArrayList<User> users)
	{
		try
		{
			XMLEncoder encoder = new XMLEncoder(new FileOutputStream(new File(System.getProperty("user.dir") + PATH + File.separator + "users.xml")));
			encoder.writeObject(users);
			encoder.close();
		}
		catch(Exception e)
		{
			LoggerUtil.logException("Greska u xml serijalizaciji", e);
			e.printStackTrace();
		}
	}
	
	private ArrayList<User> deserializeWithXML()
	{
		try
		{
			XMLDecoder decoder = new XMLDecoder(new FileInputStream(new File(System.getProperty("user.dir") + PATH + File.separator + "users.xml")));
			ArrayList<User> users = (ArrayList<User>) decoder.readObject();
			decoder.close();
			return users;
		}
		catch(Exception e)
		{
			LoggerUtil.logException("Greska u xml deserijalizaciji", e);
			e.printStackTrace();
		}
		return null;
	}
}
