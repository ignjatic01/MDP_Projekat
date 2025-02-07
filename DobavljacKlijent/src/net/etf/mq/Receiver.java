package net.etf.mq;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import net.etf.service.LoggerUtil;

public class Receiver 
{
	private final static String EXCHANGE_NAME;
    private final static String QUEUE_NAME;
    private final static String UNAME;
    private final static String PASSWORD;
    private final static String MQ_HOST;
    
    static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            EXCHANGE_NAME = prop.getProperty("EXCHANGE_NAME");
            QUEUE_NAME = prop.getProperty("QUEUE_NAME");
            UNAME = prop.getProperty("UNAME");
            PASSWORD = prop.getProperty("PASSWORD");
            MQ_HOST = prop.getProperty("MQ_HOST");
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
	
	public static String getOneMessage() 
	{
	    String message = "";
	    Connection connection = null;
	    Channel channel = null;
	
	    try 
	    {
	        ConnectionFactory factory = new ConnectionFactory();
	        factory.setHost(MQ_HOST);
	        factory.setUsername(UNAME);
	        factory.setPassword(PASSWORD);
	
	        connection = factory.newConnection();
	        channel = connection.createChannel();
	
	        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT, true);
	        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
	        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
	
	        System.out.println("Checking for old messages...");
	
	        GetResponse response = channel.basicGet(QUEUE_NAME, true);
	        if (response != null) 
	        {
	            message = new String(response.getBody(), StandardCharsets.UTF_8);
	            System.out.println("Old message received: '" + message + "'");
	        }
	    } 
	    catch (TimeoutException | IOException e) 
	    {
	    	LoggerUtil.logException("Greska u dohvatanju poruke", e);
	        e.printStackTrace();
	    } 
	    finally 
	    {
	        try 
	        {
	            if (channel != null) channel.close();
	            if (connection != null) connection.close();
	        } 
	        catch (Exception e) 
	        {
	        	LoggerUtil.logException("Greska u zatvaranju konekcije", e);
	            e.printStackTrace();
	        }
	    }
	    return message;
	}
	
	public static ArrayList<String> parseMessage(String msg)
	{
		ArrayList<String> books = new ArrayList<String>();
		String[] params = msg.split("#");
		
		return books;
	}

	public static void main(String[] args) 
	{
		System.out.println("MSG: " + getOneMessage());

	}

}
