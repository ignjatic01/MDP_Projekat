package net.etf.rmi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

import net.etf.model.Account;
import net.etf.rmi.server.AccountingServiceInterface;
import net.etf.service.LoggerUtil;


public class RMIClient 
{
	public static final String CLIENT_POLICY;
	private static final int PORT;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            CLIENT_POLICY = prop.getProperty("CLIENT_POLICY");
            PORT = Integer.parseInt(prop.getProperty("RMI_PORT"));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
	public static double sendAccount(Account acc) 
	{
		System.setProperty("java.security.policy", CLIENT_POLICY);
		/*if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());*/
		System.setProperty("java.rmi.server.useCodebaseOnly", "true");
		try
		{
			String name = "AccountingService";
			Registry registry = LocateRegistry.getRegistry(PORT);
			AccountingServiceInterface server = (AccountingServiceInterface) registry.lookup(name);
			double val = server.processAccount(acc);
			System.out.println("PDV: " + val);
			return val;
		}
		catch(Exception e)
		{
			LoggerUtil.logException("Greska u remote metodi", e);
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void main(String args[])
	{
		Account acc = new Account();
		acc.addBook("Metro 2033", 30);
		acc.addBook("Asa", 10);
		sendAccount(acc);
	}
}
