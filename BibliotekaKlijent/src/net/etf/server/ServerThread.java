package net.etf.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import net.etf.client.BookClient;
import net.etf.model.Book;

public class ServerThread extends Thread 
{
	private Socket sock;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	public ServerThread(Socket sock)
	{
		this.sock = sock;
		try
		{
			out = new ObjectOutputStream(sock.getOutputStream());
			in = new ObjectInputStream(sock.getInputStream());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void run()
	{
		String request = "";
		try 
		{
			while(!"END".equals(request))
			{
				request = (String) in.readObject();
				if("BOOKS".equals(request))
				{
					ArrayList<Book> books = (ArrayList<Book>) in.readObject();
					books.forEach(b -> System.out.println(b.getTitle()));
					Server.booksFromServer = books;
				}
				else if("GET_BOOKS".equals(request))
				{
					out.writeObject(Server.booksFromServer);
					out.flush();
				}
				else if("ORDER".equals(request))
				{
					ArrayList<Book> books = (ArrayList<Book>) in.readObject();
					books.forEach(book -> {
						System.out.println(book.getTitle() + " x " + book.getCount());
						BookClient bc = new BookClient();
						bc.addOrUpdateBook(book);
					});
				}
			}
			
			in.close();
			out.close();
			sock.close();
		}
		catch(IOException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}
