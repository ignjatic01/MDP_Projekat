package net.etf.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

import net.etf.model.Book;
import net.etf.service.LoggerUtil;

public class Server 
{
	public static final int PORT;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            PORT = Integer.parseInt(prop.getProperty("BIBLIOTEKA_C_SERVER_PORT"));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	public static ArrayList<Book> booksFromServer = new ArrayList<Book>();
	
	public static void main(String args[])
	{
		try
		{
			ServerSocket ss = new ServerSocket(PORT);
			System.out.println("Server started");
			while(true)
			{
				Socket sock = ss.accept();
				new ServerThread(sock).start();
			}
		}
		catch(IOException e)
		{
			LoggerUtil.logException("Greska u pokretanju servera", e);
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Book> getBooks()
	{
		return booksFromServer;
	}
}
