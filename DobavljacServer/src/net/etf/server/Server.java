package net.etf.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.etf.model.Book;
import net.etf.service.BookService;
import net.etf.service.LoggerUtil;

public class Server 
{
	public static final String PATH;
	public static final int PORT;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            PATH = prop.getProperty("LINK_PATH");
            PORT = Integer.parseInt(prop.getProperty("PORT"));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
	public static ArrayList<Book> books = new ArrayList<Book>();

	public static void main(String[] args) 
	{
		BookService service = new BookService();
		List<String> links = new ArrayList<String>();
		try 
		{
			links = Files.readAllLines(Path.of(PATH));
		} 
		catch (IOException e) 
		{
			LoggerUtil.logException("Greska u ucitavanju knjiga", e);
			e.printStackTrace();
		}
		links.forEach(link -> {
			Book temp;
			temp = BookService.getBook(link);
			books.add(temp);
		});
		
		try 
		{
			ServerSocket ss = new ServerSocket(9001);
			System.out.println("Server started");
			while(true)
			{
				Socket socket = ss.accept();
				new ServerThread(socket).start();
			}
		} 
		catch (IOException e) 
		{
			LoggerUtil.logException("Greska u komunikaciji", e);
			e.printStackTrace();
		}
	}

}
