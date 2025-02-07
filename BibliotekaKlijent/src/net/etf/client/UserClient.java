package net.etf.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import net.etf.model.User;
import net.etf.service.LoggerUtil;

public class UserClient {
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

    public UserClient() 
    {
        client = ClientBuilder.newClient();
    }

    public String login(String username, String password) 
    {
        target = client.target(BASE_URL);
        User user = new User(username, password);
        return target.request(MediaType.APPLICATION_JSON)
                .post(javax.ws.rs.client.Entity.entity(user, MediaType.APPLICATION_JSON), String.class);
    }

    public String register(User user) 
    {
        target = client.target(BASE_URL + "/register");
        return target.request(MediaType.APPLICATION_JSON)
                .post(javax.ws.rs.client.Entity.entity(user, MediaType.APPLICATION_JSON), String.class);
    }

    public String allowUser(String username) 
    {
        target = client.target(BASE_URL + "/allow/" + username);
        return target.request(MediaType.APPLICATION_JSON)
                .put(javax.ws.rs.client.Entity.json(""), String.class);
    }

    public ArrayList<User> getNotAllowedUsers() 
    {
        target = client.target(BASE_URL + "/notallowed");
        return target.request(MediaType.APPLICATION_JSON)
                .get(new javax.ws.rs.core.GenericType<ArrayList<User>>() {});
    }

    public ArrayList<User> getAllowedUsers() 
    {
        target = client.target(BASE_URL + "/allowed");
        return target.request(MediaType.APPLICATION_JSON)
                .get(new javax.ws.rs.core.GenericType<ArrayList<User>>() {});
    }
    
    public String blockUser(String username) 
    {
        target = client.target(BASE_URL + "/block/" + username);
        return target.request(MediaType.APPLICATION_JSON)
                .put(javax.ws.rs.client.Entity.json(""), String.class);
    }

    public String unblockUser(String username) 
    {
        target = client.target(BASE_URL + "/unblock/" + username);
        return target.request(MediaType.APPLICATION_JSON)
                .put(javax.ws.rs.client.Entity.json(""), String.class);
    }

    public String deleteUser(String username) 
    {
        target = client.target(BASE_URL + "/" + username);
        return target.request(MediaType.APPLICATION_JSON)
                .delete(String.class);
    }

    public void close() 
    {
        client.close();
    }

    public static void main(String[] args) {
        UserClient client = new UserClient();

        // Testiranje registracije
        User newUser = new User("John", "Doe", "123 Street", "john@example.com", "johndoe", "password");
        System.out.println("Register response: " + client.register(newUser));

        // Testiranje prijave
        System.out.println("Login response: " + client.login("johndoe", "password"));

        // Dozvoljavanje korisnika
        System.out.println("Allow user response: " + client.allowUser("johndoe"));

        // Dohvatanje neodobrenih korisnika
        System.out.println("Not allowed users: " + client.getNotAllowedUsers());

        // Dohvatanje odobrenih korisnika
        System.out.println("Allowed users: " + client.getAllowedUsers());

        client.close();
    }
}
