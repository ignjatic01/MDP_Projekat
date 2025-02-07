package net.etf.client;

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
import net.etf.model.Image;
import net.etf.service.LoggerUtil;

import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class BookClient {

    private static final String BASE_URL;
    private Client client;
    private Gson gson;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            BASE_URL = prop.getProperty("BASE_URL_BOOKS");
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}

    public BookClient() 
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

    public boolean addOrUpdateBook(Book book) 
    {
        WebTarget target = client.target(BASE_URL);
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);

        String jsonBook = gson.toJson(book); 
        Response response = request.post(Entity.entity(jsonBook, MediaType.APPLICATION_JSON));
        return response.getStatus() == 200;
    }
    
    public boolean deleteBook(long id) 
    {
        WebTarget target = client.target(BASE_URL).path(String.valueOf(id));
        System.out.println(target.getUri());
        Invocation.Builder request = target.request();

        Response response = request.delete();
        return response.getStatus() == 200;
    }

    public static void main(String[] args) 
    {
        BookClient client = new BookClient();


        List<Book> books = client.getBooks();
        if (books != null) {
            books.forEach(book -> System.out.println(book.getTitle()));
        } else {
            System.out.println("Nema dostupnih knjiga!");
        }

        // 2️⃣ Test POST - Dodaj novu knjigu
        /*Book newBook = new Book("Test Knjiga", 101, "Autor X", "2024-01-01", "SR", new Image());
        newBook.setFileContent("Sadrzaj knjige".getBytes());
        boolean added = client.addOrUpdateBook(newBook);
        System.out.println("➕ Knjiga dodata: " + added);

        // 3️⃣ Test DELETE - Briši knjigu
        boolean deleted = client.deleteBook(101);
        System.out.println("❌ Knjiga obrisana: " + deleted);*/
    }
}
