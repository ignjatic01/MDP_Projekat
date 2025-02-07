package net.etf.test;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import net.etf.model.User;

public class Test {
	
	public static final String PATH = "resources";

	public static void main(String[] args) 
	{
		ArrayList<User> users = new ArrayList<>();
		users.add(new User("Marko", "Markovic", "Srpska", "marko@mail.com", "marko123", "pass"));
		users.add(new User("Ana", "Anic", "Srpskih pilota", "ana@mail.com", "ana1", "pass"));
		serializeWithXML(users);
		
		ArrayList<User> loaded = deserializeWithXML();
		loaded.forEach(System.out::println);
	}
	
	public static void serializeWithXML(ArrayList<User> users)
	{
		try
		{
			XMLEncoder encoder = new XMLEncoder(new FileOutputStream(new File(PATH + File.separator + "users.xml")));
			encoder.writeObject(users);
			encoder.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static ArrayList<User> deserializeWithXML()
	{
		try
		{
			XMLDecoder decoder = new XMLDecoder(new FileInputStream(new File(PATH + File.separator + "users.xml")));
			ArrayList<User> users = (ArrayList<User>) decoder.readObject();
			decoder.close();
			return users;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
