package net.etf.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.etf.model.User;
import net.etf.service.LoggerUtil;

public class BibliotekaClient 
{
	private static final String BASE_URL;
    private Client client;
    private WebTarget target;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            BASE_URL = prop.getProperty("BASE_URL_USERS");
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
    
    public BibliotekaClient() 
    {
        client = ClientBuilder.newClient();
    }
	
	public String login(String username, String password) 
	{
        target = client.target(BASE_URL);
        User user = new User(username, password);
        try 
        {
            return target.request(MediaType.APPLICATION_JSON)
                    .post(javax.ws.rs.client.Entity.entity(user, MediaType.APPLICATION_JSON), String.class);
        } 
        catch (javax.ws.rs.NotAuthorizedException e) 
        {
            return "NOK";
        }
    }
	
	public String getUserEmail(String username) 
	{
        target = client.target(BASE_URL).path("email").path(username);

        try 
        {
            return target.request(MediaType.APPLICATION_JSON).get(String.class);
        } 
        catch (javax.ws.rs.ClientErrorException e) 
        {
            int status = e.getResponse().getStatus();
            if (status == 404) 
            {
                return "USER_NOT_FOUND";
            }
            return "ERROR";
        } 
        catch (Exception e)
        {
        	LoggerUtil.logException("Greska u dohvatanju mejla", e);
            return "ERROR";
        }
    }
	
	public String register(User user) 
	{
	    target = client.target(BASE_URL).path("/register");

	    try 
	    {
	        return target.request(MediaType.APPLICATION_JSON)
	                .post(javax.ws.rs.client.Entity.entity(user, MediaType.APPLICATION_JSON), String.class);
	    } 
	    catch (javax.ws.rs.ClientErrorException e) 
	    {
	        int status = e.getResponse().getStatus();
	        if (status == 400) 
	        {
	            return "BAD_REQUEST";
	        } 
	        else if (status == 409) 
	        {
	            return "USERNAME_EXISTS";
	        }
	        return "ERROR";
	    } 
	    catch (Exception e) 
	    {
	    	LoggerUtil.logException("Greska u registraciji", e);
	        return "ERROR"; 
	    }
	}

}
