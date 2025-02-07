package net.etf.chat.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import net.etf.service.LoggerUtil;

public class ServerThread extends Thread 
{
	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;
	
	public ServerThread(Socket sock)
	{
		this.sock = sock;
		try
		{
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);
		}
		catch(IOException e)
		{
			LoggerUtil.logException("Greska u pokretanju niti", e);
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		try
		{
			String req = "";
			while(!"END".equals(req))
			{
				req = in.readLine();
				if(req.startsWith("REGISTER"))
				{
					String params[] = req.split("#");
					if (!Server.users.contains(params[1])) {
					    Server.users.add(params[1]);
					}
					out.println("OK");
				}
				else if("GET_U".equals(req))
				{
					StringBuilder res = new StringBuilder();
					for(String p : Server.users)
					{
						res.append(p + "#");
					}
					out.println(res.toString());
				}
				else if(req.startsWith("MSG"))
				{
					String params[] = req.split("#");
					Server.messages.add(params[1] + "-" + params[2] + ": " + params[3]);
					out.println("OK");
				}
				else if(req.startsWith("GET_M"))
				{
					String params[] = req.split("#");
					StringBuilder res = new StringBuilder();
					for(String msg : Server.messages)
					{
						if(msg.startsWith(params[1]))
						{
							res.append(msg.split("-")[1] + "#");
						}
					}
					out.println(res.toString());
				}
				else
				{
					out.println("ERR");
				}
			}
			out.close();
			in.close();
			sock.close();
		}
		catch(Exception e)
		{
			LoggerUtil.logException("Greska u komunikaciji", e);
			e.printStackTrace();
		}
	}
}
