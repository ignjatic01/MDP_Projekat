package net.etf.multicast;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Properties;

import net.etf.service.LoggerUtil;

public class MulticastClient 
{
	public static final String MULTICAST_ADDRESS;
	public static final int PORT;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            MULTICAST_ADDRESS = prop.getProperty("MULTICAST_ADDRESS");
            PORT = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
	public void sendMessage(String msg)
	{
		try(MulticastSocket sock = new MulticastSocket(PORT))
		{
			InetAddress addr = InetAddress.getByName(MULTICAST_ADDRESS);
			byte[] buffer = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, addr, PORT);
			sock.send(packet);
		}
		catch(IOException e)
		{
			LoggerUtil.logException("Greska u slanju multicast poruke", e);
			e.printStackTrace();
		}
	}
}
