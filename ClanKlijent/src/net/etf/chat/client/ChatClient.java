package net.etf.chat.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import net.etf.service.LoggerUtil;

public class ChatClient 
{
    private static final String SECURE_SERVER_HOST;
    private static final int SECURE_SERVER_PORT;
    private static final String KEY_STORE_PATH;
	private static final String KEY_STORE_PASS;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            SECURE_SERVER_HOST = prop.getProperty("SECURE_SERVER_HOST");
            KEY_STORE_PATH = prop.getProperty("KEY_STORE_PATH");
            KEY_STORE_PASS = prop.getProperty("KEY_STORE_PASS");
            SECURE_SERVER_PORT = Integer.parseInt(prop.getProperty("SECURE_SERVER_PORT"));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
    
    private SSLSocket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    public void init()
    {
    	System.setProperty("javax.net.ssl.trustStore", KEY_STORE_PATH);
    	System.setProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASS);
    	try
    	{
    		InetAddress addr = InetAddress.getByName(SECURE_SERVER_HOST);
    		SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
    		socket = (SSLSocket) sf.createSocket(addr, SECURE_SERVER_PORT);
    		
    		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    	
			/*out.println("REGISTER#" + 1);
        	System.out.println(in.readLine());*/
    	}
    	catch(IOException e)
    	{
    		LoggerUtil.logException("Greska u inicijalizaciji klijenta", e);
    		e.printStackTrace();
    	}
    }
    
    public void register(String id)
    {
    	try
    	{
    		out.println("REGISTER#" + id);
        	System.out.println(in.readLine());
    	}
    	catch(IOException e)
    	{
    		LoggerUtil.logException("Greska u registraciji", e);
    		e.printStackTrace();
    	}
    }
    
    public void getUsers(TextArea tau)
    {
    	try
    	{
    		tau.setText("");
    		out.println("GET_U");
        	String resp = in.readLine();
        	String[] users = resp.split("#");
        	StringBuilder sb = new StringBuilder();
        	for(String user : users)
        	{
        		sb.append(user + "\n");
        	}
        	Platform.runLater(() -> {
                tau.appendText(sb.toString());
            });
    	}
    	catch(IOException e)
    	{
    		LoggerUtil.logException("Greska u ucitavanju korisnika", e);
    		e.printStackTrace();
    	}
    }
    
    public void sendMessage(String idReciever, String idSender,  String msg)
    {
    	try
    	{
    		out.println("MSG#" + idReciever + "#" + idSender + "#" + msg);
        	System.out.println(in.readLine());
    	}
    	catch(IOException e)
    	{
    		LoggerUtil.logException("Greska u slanju poruke", e);
    		e.printStackTrace();
    	}
    }
    
    public void getMessages(String id, TextArea tai)
    {
    	try
    	{
    		tai.setText("");
    		out.println("GET_M#" + id);
        	String resp = in.readLine();
        	String[] msgs = resp.split("#");
        	StringBuilder sb = new StringBuilder();
        	for(String msg : msgs)
        	{
        		sb.append(msg + "\n");
        	}
        	Platform.runLater(() -> {
                tai.appendText(sb.toString());
            });
    	}
    	catch(IOException e)
    	{
    		LoggerUtil.logException("Greska u ucitavanju poruka", e);
    		e.printStackTrace();
    	}
    }
    
    public void end()
    {
    	out.println("END");
    	this.close();
    }
    
    private void close()
    {
    	try
    	{
    		out.close();
    		in.close();
    		socket.close();
    	}
    	catch(IOException e)
    	{
    		LoggerUtil.logException("Greska u zatvaranju konekcije", e);
    		e.printStackTrace();
    	}
    }
    
    public static void main(String args[])
    {
    	/*String user1 = "Jurich";
    	String user2 = "Bart";
    	ChatClient cc = new ChatClient();
    	cc.init();
    	cc.register(user1);
    	cc.register(user2);
    	cc.getUsers();
    	cc.sendMessage(user1, user2, "Ovo je testna poruka");
    	cc.getMessages(user1);
    	cc.end();*/
    }
}