package net.etf.mq;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import net.etf.service.LoggerUtil;

public class Sender 
{
	private final static String EXCHANGE_NAME;
    private final static String UNAME;
    private final static String PASSWORD;
    private final static String MQ_HOST;
    
    static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            EXCHANGE_NAME = prop.getProperty("EXCHANGE_NAME");
            UNAME = prop.getProperty("UNAME");
            PASSWORD = prop.getProperty("PASSWORD");
            MQ_HOST = prop.getProperty("MQ_HOST");
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
	public static void sendMessage(String message)
	{
		try
		{
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(MQ_HOST);
	        factory.setUsername(UNAME);
	        factory.setPassword(PASSWORD);
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			
			channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT, true);

	        //Scanner in = new Scanner(System.in);
	        
	        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .deliveryMode(2)
                    .build();
            
            channel.basicPublish(EXCHANGE_NAME, "", properties, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Sent: '" + message + "'");

	        //in.close();
	        channel.close();
	        connection.close();
		}
		catch(TimeoutException | IOException e)
		{
			LoggerUtil.logException("Greska u slanju poruke", e);
			e.printStackTrace();
		}
	}
}
