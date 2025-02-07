package net.etf.chat.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Properties;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import net.etf.service.LoggerUtil;


public class Server 
{
    private static final int SECURE_SERVER_PORT;
    private static final String KEY_STORE_PATH;
	private static final String KEY_STORE_PASS;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            KEY_STORE_PATH = prop.getProperty("KEY_STORE_PATH");
            KEY_STORE_PASS = prop.getProperty("KEY_STORE_PASS");
            SECURE_SERVER_PORT = Integer.parseInt(prop.getProperty("SECURE_SERVER_PORT"));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
	public static ArrayList<String> users = new ArrayList<String>();
	public static ArrayList<String> messages = new ArrayList<String>();
	
	public static void main(String[] args)
	{
		System.setProperty("javax.net.ssl.keyStore", KEY_STORE_PATH);
		System.setProperty("javax.net.ssl.keyStorePassword", KEY_STORE_PASS);
		
		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		try
		{
			ServerSocket ss = (SSLServerSocket) ssf.createServerSocket(SECURE_SERVER_PORT);
			System.out.println("Server started.");
			while(true)
			{
				SSLSocket sock = (SSLSocket) ss.accept();
				new ServerThread(sock).start();
			}
		}
		catch(IOException e)
		{
			LoggerUtil.logException("Greska u pokretanju sigurnog servera", e);
			e.printStackTrace();
		}
	}
}
