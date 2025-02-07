package net.etf.dobavljac.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import net.etf.model.Book;
import net.etf.service.LoggerUtil;

public class Client 
{
	public static final String ADDRESS;
	public static final int PORT;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            ADDRESS = prop.getProperty("DOBAVLJAC_SERVER_ADDRESS");
            PORT = Integer.parseInt(prop.getProperty("DOBAVLJAC_SERVER_PORT"));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
	public ObjectOutputStream out;
	public ObjectInputStream in;
	
	public ArrayList<Book> connect()
	{
		ArrayList<Book> books = new ArrayList<Book>();
		try 
		{
			InetAddress add = InetAddress.getByName(ADDRESS);
			Socket socket = new Socket(add, PORT);
			
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			
			out.writeObject("BOOKS");
			out.flush();
			
			books = (ArrayList<Book>) in.readObject();
			
			out.writeObject("END");
			out.flush();
			
			//books.forEach(book -> System.out.println(new String(book.getFirstPage().getImageData())));
			
			in.close();
			out.close();
			socket.close();
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			LoggerUtil.logException("Greska u komunikaciji", e);
			e.printStackTrace();
		}
		return books;
	}
	
	public ArrayList<String> getSpecificBookNames(String request)
	{
		ArrayList<String> bookNames = new ArrayList<String>();
		try 
		{
			InetAddress add = InetAddress.getByName(ADDRESS);
			Socket socket = new Socket(add, PORT);
			
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			
			out.writeObject(request);
			out.flush();
			
			bookNames = (ArrayList<String>) in.readObject();
			
			out.writeObject("END");
			out.flush();
			
			//books.forEach(book -> System.out.println(new String(book.getFirstPage().getImageData())));
			
			in.close();
			out.close();
			socket.close();
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			LoggerUtil.logException("Greska u komunikaciji", e);
			e.printStackTrace();
		}
		return bookNames;
	}
	
	public ArrayList<Book> getSpecificBooks(String request)
	{
		ArrayList<Book> books = new ArrayList<Book>();
		try 
		{
			InetAddress add = InetAddress.getByName(ADDRESS);
			Socket socket = new Socket(add, PORT);
			
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			
			out.writeObject(request);
			out.flush();
			
			books = (ArrayList<Book>) in.readObject();
			
			out.writeObject("END");
			out.flush();
			
			//books.forEach(book -> System.out.println(new String(book.getFirstPage().getImageData())));
			
			in.close();
			out.close();
			socket.close();
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			LoggerUtil.logException("Greska u komunikaciji", e);
			e.printStackTrace();
		}
		return books;
	}
	
	public Book addBook(String link)
	{
		Book book = new Book();
		try 
		{
			InetAddress add = InetAddress.getByName(ADDRESS);
			Socket socket = new Socket(add, PORT);
			
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			
			out.writeObject("ADD");
			out.writeObject(link);
			out.flush();
			
			book = (Book) in.readObject();
			
			out.writeObject("END");
			out.flush();
			
			//books.forEach(book -> System.out.println(new String(book.getFirstPage().getImageData())));
			
			in.close();
			out.close();
			socket.close();
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			LoggerUtil.logException("Greska u komunikaciji", e);
			e.printStackTrace();
		}
		return book;
	}
	
	public static void main(String[] args) 
	{
		Client cl = new Client();
		ArrayList<Book> books = cl.getSpecificBooks("GET_BOOKS#2554,12#1727,40");
		books.forEach(b -> System.out.println(b.getFileContent().length));
	}

}
