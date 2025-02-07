package net.etf.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.etf.model.Book;
import net.etf.service.LoggerUtil;

public class BooksClient 
{
	private static final String BASE_URL;
	public static final String PATH;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            BASE_URL = prop.getProperty("BASE_URL_BOOKS");
            PATH = prop.getProperty("BOOKS_PATH");
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
    private Client client;
    private Gson gson;

    public BooksClient() 
    {
        client = ClientBuilder.newClient();
        gson = new GsonBuilder().create();
    }

    public List<Book> getBooks() 
    {
        WebTarget target = client.target(BASE_URL);
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);

        Response response = request.get();
        if (response.getStatus() == 200) 
        {
            String jsonResponse = response.readEntity(String.class);
            Book[] books = gson.fromJson(jsonResponse, Book[].class);
            return Arrays.asList(books);
        }
        return null;
    }
    
    public List<String> getFirst100(long id)
    {
    	WebTarget target = client.target(BASE_URL).path("firstlines").path(String.valueOf(id));
    	Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);
    	
    	Response response = request.get();
    	if(response.getStatus() == 200)
    	{
    		String jsonResponse = response.readEntity(String.class);
    		String[] strings = gson.fromJson(jsonResponse, String[].class);
    		return Arrays.asList(strings);
    	}
    	
    	return null;
    }
    
    public List<Book> getBooksNoContent() 
    {
        WebTarget target = client.target(BASE_URL).path("nocontent");
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);

        Response response = request.get();
        if (response.getStatus() == 200) 
        {
            String jsonResponse = response.readEntity(String.class);
            Book[] books = gson.fromJson(jsonResponse, Book[].class);
            return Arrays.asList(books);
        }
        return null;
    }

    public boolean addOrUpdateBook(Book book) 
    {
        WebTarget target = client.target(BASE_URL);
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);

        String jsonBook = gson.toJson(book);
        Response response = request.post(Entity.entity(jsonBook, MediaType.APPLICATION_JSON));
        return response.getStatus() == 200;
    }
    
    public void downloadBook(long id) 
    {
        WebTarget target = client.target(BASE_URL).path("download").path(String.valueOf(id));
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);

        Response response = request.get();
        if (response.getStatus() == 200) 
        {
            String jsonResponse = response.readEntity(String.class);
            Book temp = gson.fromJson(jsonResponse, Book.class);

            if (temp != null && temp.getFileContent() != null) 
            {
                try 
                {
                    String fileContent = new String(temp.getFileContent(), StandardCharsets.UTF_8);
                    
                    String filePath = Paths.get(PATH, id + ".txt").toString();
                    File file = new File(filePath);
                    
                    // Upisujemo podatke u fajl
                    try (FileOutputStream fos = new FileOutputStream(file)) 
                    {
                        fos.write(fileContent.getBytes(StandardCharsets.UTF_8));
                    }
                    
                    System.out.println("Knjiga sacuvana na putanji: " + filePath);
                } 
                catch (IOException e) 
                {
                	LoggerUtil.logException("Greska u upisu u fajl", e);
                }
            } 
            else 
            {
                System.out.println("Nema sadrzaj");
            }
        } else 
        {
            System.out.println("HTTP kod: " + response.getStatus());
        }
    }
    

    public boolean deleteBook(long id) 
    {
        WebTarget target = client.target(BASE_URL).path(String.valueOf(id));
        //System.out.println(target.getUri());
        Invocation.Builder request = target.request();

        Response response = request.delete();
        return response.getStatus() == 200;
    }
}
