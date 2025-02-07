package net.etf.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import net.etf.model.Account;



public interface AccountingServiceInterface extends Remote
{
	public double processAccount(Account account) throws RemoteException;
}
