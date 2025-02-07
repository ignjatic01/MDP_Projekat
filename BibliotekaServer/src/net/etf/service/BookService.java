package net.etf.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.etf.model.Book;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class BookService 
{
	
	private static final String REDIS_HOST;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(System.getProperty("user.dir") + File.separator + "src" + File.separator + "resources/config.properties")) {
            prop.load(input);
            REDIS_HOST = prop.getProperty("REDIS_HOST");
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
	private final JedisPool pool = new JedisPool(REDIS_HOST);
	
	public BookService()
	{
		
	}
	
	/*public boolean addBook(Book book)
	{
		try (Jedis jedis = pool.getResource()) {
            String key = "book:" + book.getId();

            // 🔹 Rucna serijalizacija (pretvaranje objekta u String)
            String value = book.getTitle() + "|" + book.getId() + "|" + book.getAuthor() + "|" +
                           book.getPublishDate() + "|" + book.getLanguage() + "|" + 
                           Base64.getEncoder().encodeToString(book.getFileContent()) + "|" +
                           book.getCount();

            jedis.set(key, value);
            
            jedis.lpush("books:all_ids", String.valueOf(book.getId()));
            
            System.out.println("Book saved: " + key);
        }
		return true;
	}*/
	
	public Book getBook(long id) 
	{
        try (Jedis jedis = pool.getResource()) 
        {
            String key = "book:" + id;
            String value = jedis.get(key);

            if (value != null) 
            {
                String[] parts = value.split("\\|");

                Book book = new Book();
                book.setTitle(parts[0]);
                book.setId(Long.parseLong(parts[1]));
                book.setAuthor(parts[2]);
                book.setPublishDate(parts[3]);
                book.setLanguage(parts[4]);
                book.setImageLink(parts[5]);
                book.setFileContent(Base64.getDecoder().decode(parts[6]));
                book.setCount(Integer.parseInt(parts[7]));

                return book;
            }
        }
        return null;
    }
	
	
	
	public ArrayList<Book> getBooks()
	{
		ArrayList<Book> books = new ArrayList<Book>();
		
		try (Jedis jedis = pool.getResource()) 
		{
		    Set<String> bookIds = jedis.smembers("books:all_ids");

		    for (String id : bookIds) 
		    {
		        Book book = getBook(Long.parseLong(id));
		        if (book != null && book.getCount() > 0) 
		        {
		            books.add(book);
		        }
		    }
		}


	    return books;
	}
	
	public ArrayList<Book> getBooksNoContent()
	{
		ArrayList<Book> books = new ArrayList<Book>();
		
		try (Jedis jedis = pool.getResource()) 
		{
		    Set<String> bookIds = jedis.smembers("books:all_ids");

		    for (String id : bookIds) 
		    {
		        Book book = getBook(Long.parseLong(id));
		        if (book != null) 
		        {
		        	book.setFileContent("".getBytes());
		            books.add(book);
		        }
		    }
		}
	    return books;
	}
	
	public ArrayList<String> getFirst100(long id) 
	{
	    ArrayList<String> lines = new ArrayList<>();

	    try (Jedis jedis = pool.getResource()) 
	    {
	        String key = "book:" + id;
	        String value = jedis.get(key);

	        if (value != null) {
	            String[] parts = value.split("\\|");

	            Book book = new Book();
	            book.setTitle(parts[0]);
	            book.setId(Long.parseLong(parts[1]));
	            book.setAuthor(parts[2]);
	            book.setPublishDate(parts[3]);
	            book.setLanguage(parts[4]);
	            book.setImageLink(parts[5]);
	            book.setFileContent(Base64.getDecoder().decode(parts[6]));

	            // Čitanje prvih 100 linija
	            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(book.getFileContent())))) 
	            {
	                String line;
	                int count = 0;
	                while ((line = reader.readLine()) != null && count < 100) 
	                {
	                    lines.add(line);
	                    count++;
	                }
	            }
	        }
	    } 
	    catch (Exception e) 
	    {
	    	LoggerUtil.logException("Greska u citanju knjige", e);
	        e.printStackTrace();
	    }

	    return lines;
	}
	
	public boolean updateBook(Book book)
	{
		return true;
	}
	
	public boolean addOrUpdateBook(Book book)
	{
		try (Jedis jedis = pool.getResource()) 
		{
			String key = "book:" + book.getId();
            String value = jedis.get(key);

            String value2 = book.getTitle() + "|" + book.getId() + "|" + book.getAuthor() + "|" +
                    book.getPublishDate() + "|" + book.getLanguage() + "|" + book.getImageLink() + "|" +
                    Base64.getEncoder().encodeToString(book.getFileContent()) + "|" +
                    book.getCount();

            jedis.set(key, value2);
            
            jedis.sadd("books:all_ids", String.valueOf(book.getId()));

            
            System.out.println("Book saved: " + key);
        }
		return true;
	}
	
	public boolean addOrUpdateBookList(ArrayList<Book> books)
	{
		return true;
	}
	
	public static void main(String[] args) {
        BookService service = new BookService();
        
        //service.deleteBook(2);

        // Kreiramo knjigu
        /*Book book = new Book("Redis in Action 1.1", 1L, "Josiah L. Carlson",
                "2013-11-01", "English", null, 3);
        book.setFileContent("Ovo je testni sadrzaj knjige...".getBytes());

        // Čuvamo knjigu u Redis
        service.addOrUpdateBook(book);
        
        book = new Book("Redis in Action 3", 3L, "Josiah L. Carlson",
                "2013-11-01", "English", null, 3);
        book.setFileContent("Ovo je testni sadrzaj knjige...".getBytes());

        // Čuvamo knjigu u Redis
        service.addOrUpdateBook(book);*/
        
        List<Book> books = service.getBooks();
        books.forEach(b -> System.out.println(b.getTitle()));
    }
	
	public boolean deleteBook(long id) 
	{
	    try (Jedis jedis = pool.getResource()) 
	    {
	        if (!jedis.sismember("books:all_ids", String.valueOf(id))) 
	        {
	            return false;
	        }

	        jedis.del("book:" + id);

	        jedis.srem("books:all_ids", String.valueOf(id));

	        return true;
	    } 
	    catch (Exception e) 
	    {
	    	LoggerUtil.logException("Greska u brisanju knjige", e);
	        e.printStackTrace();
	        return false;
	    }
	}

}
