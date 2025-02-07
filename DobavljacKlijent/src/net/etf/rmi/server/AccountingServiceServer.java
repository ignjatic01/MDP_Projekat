package net.etf.rmi.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;

import net.etf.model.Account;
import net.etf.service.LoggerUtil;


public class AccountingServiceServer implements AccountingServiceInterface 
{
	public static final String SERVER_POLICY;
	public static final String SERIALIZE_PATH;
	private static final int PORT;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            SERVER_POLICY = prop.getProperty("SERVER_POLICY");
            SERIALIZE_PATH = prop.getProperty("SERIALIZE_PATH");
            PORT = Integer.parseInt(prop.getProperty("RMI_PORT"));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}

	@Override
	public double processAccount(Account account) throws RemoteException 
	{
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SERIALIZE_PATH + File.separator + System.currentTimeMillis() + ".out"))) {
            oos.writeObject(account);
            System.out.println("Racun serijalizovan");
        } catch (IOException e) {
        	LoggerUtil.logException("Greska u serijalizaciji racuna", e);
            e.printStackTrace();
        }
		double ret = account.getCijena() * 17 / 100;
		return ret;
	}

	public static void main(String[] args) 
	{
		System.setProperty("java.securty.policy", SERVER_POLICY);
		/*if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());*/
		System.setProperty("java.rmi.server.useCodebaseOnly", "true");
		
		try
		{
			AccountingServiceServer serv = new AccountingServiceServer();
			AccountingServiceInterface stub = (AccountingServiceInterface) UnicastRemoteObject.exportObject(serv, 0);
			Registry reg = LocateRegistry.createRegistry(PORT);
			reg.rebind("AccountingService", stub);
			System.out.println("Server pokrenut.");
		}
		catch(RemoteException e)
		{
			LoggerUtil.logException("Greska pri objavljivanju remote objekta", e);
			e.printStackTrace();
		}
	}

}
