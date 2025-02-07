package net.etf.multicast;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Properties;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import net.etf.service.LoggerUtil;

public class MulticastServer extends Thread
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
	
	
	private TextArea txtArea;
	
	public void run()
	{
		System.out.println("Multicast server pokrenut.");
        try (MulticastSocket socket = new MulticastSocket(PORT)) 
        {
            InetAddress address = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(address);

            byte[] buffer = new byte[1024];
            while (true) 
            {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); 

                String message = new String(packet.getData(), 0, packet.getLength());

                Platform.runLater(() -> {
                    txtArea.appendText(message + "\n");
                });
            }
        } 
        catch (IOException e) 
        {
        	LoggerUtil.logException("Greska u multicast komunikaciji", e);
            e.printStackTrace();
        }
	}
	
	/*public static void main(String args[])
	{
		
	}*/
}
