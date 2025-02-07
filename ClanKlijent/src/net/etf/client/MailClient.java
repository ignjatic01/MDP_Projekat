package net.etf.client;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.etf.model.Book;
import net.etf.service.LoggerUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MailClient {

    private static final String BASE_URL;
    
    static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            BASE_URL = prop.getProperty("BASE_URL_EMAIL");
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}

    public static String sendMail(List<Book> books, String email) 
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(BASE_URL).path(email);

        Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(books, MediaType.APPLICATION_JSON));

        String result = response.readEntity(String.class);
        response.close();
        return result;
    }
}

