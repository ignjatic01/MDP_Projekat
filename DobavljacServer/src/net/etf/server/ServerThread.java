package net.etf.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import net.etf.model.Book;
import net.etf.service.BookService;
import net.etf.service.LoggerUtil;

public class ServerThread extends Thread 
{
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public ServerThread(Socket socket)
	{
		this.socket = socket;
		try
		{
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
		}
		catch(IOException e)
		{
			LoggerUtil.logException("Greska u uspostavljanju komunikacije sa klijentom", e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void run()
	{
		try
		{
			String request = "";
			while(!"END".equals(request))
			{
				request = (String) in.readObject();
				if("BOOKS".equals(request))
				{
						out.writeObject(Server.books);
				}
				else if("ADD".equals(request))
				{
					String link = (String) in.readObject();
					Book temp = BookService.getBook(link);
					Server.books.add(temp);
					out.writeObject(temp);
				}
				else if(request.startsWith("GET_BOOKS#"))
				{
					String[] params = request.split("#");
					ArrayList<Book> specificBooks = new ArrayList<Book>();
					for(int i = 1; i < params.length; i++)
					{
						System.out.println(params[i].split(",")[1]);
						for(Book b : Server.books)
						{
							if(Integer.parseInt(params[i].split(",")[0]) == b.getId())
							{
								specificBooks.add(b);
							}
						}
					}
					out.writeObject(specificBooks);
				}
				else if(request.startsWith("NAMES_GET_BOOKS#"))
				{
					String[] params = request.split("#");
					ArrayList<String> specificBookNames = new ArrayList<>();
					for(int i = 1; i < params.length; i++)
					{
						for(Book b : Server.books)
						{
							if(Integer.parseInt(params[i].split(",")[0]) == b.getId())
							{
								specificBookNames.add(b.getTitle() + " x " + params[i].split(",")[1]);
							}
						}
					}
					out.writeObject(specificBookNames);
				}
				out.flush();
			}
			
			out.close();
			in.close();
			socket.close();
		}
		catch(IOException | ClassNotFoundException e)
		{
			LoggerUtil.logException("Greska u komunikaciji", e);
			e.printStackTrace();
		}
	}
}
