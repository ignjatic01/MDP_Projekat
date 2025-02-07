package net.etf.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

import net.etf.model.Book;
import net.etf.service.LoggerUtil;

public class LocalClient 
{
	public static final String ADDRESS;
	public static final int PORT;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            ADDRESS = prop.getProperty("BIBLIOTEKA_C_SERVER_ADDRESS");
            PORT = Integer.parseInt(prop.getProperty("BIBLIOTEKA_C_SERVER_PORT"));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
	public ObjectOutputStream out;
	public ObjectInputStream in;
	
	public static ArrayList<Book> books = new ArrayList<Book>();
	
	public ArrayList<Book> getBooks()
	{
		try 
		{
			InetAddress add = InetAddress.getByName(ADDRESS);
			Socket socket = new Socket(add, PORT);
			
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			
			out.writeObject("GET_BOOKS");
			out.flush();
			
			books = (ArrayList<Book>) in.readObject();
			
			//books.forEach(b -> System.out.println(b.getAuthor()));
			
			out.writeObject("END");
			out.flush();
			
			//books.forEach(book -> System.out.println(new String(book.getFirstPage().getImageData())));
			
			in.close();
			out.close();
			socket.close();
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			LoggerUtil.logException("Greska u dohvatanju knjiga", e);
			e.printStackTrace();
		}
		return books;
	}
}
