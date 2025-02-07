package net.etf.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Properties;

import net.etf.model.Book;
import net.etf.model.Image;

public class BookService
{
	private static final String PATH;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) 
		{
            prop.load(input);
            PATH = prop.getProperty("BOOK_PATH");
        } 
		catch (IOException e) 
		{
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne moze se ucitati konfiguracija!");
        }
	}
	
	public static Book getBook(String link)
	{
		File folder = new File(PATH);
		if(!folder.exists())
		{
			folder.mkdir();
		}
		String numberStr = link.replaceAll(".*?(\\d+).*", "$1");
		long id = Long.parseLong(numberStr);
		String filePath = PATH + File.separator + id + ".txt";
		File file = new File(filePath);
		String title = "";
		String author = "";
		String publishDate = "";
		String language = "";
		byte[] fileContent = null;
		try 
		{
			file.createNewFile();
			URL url = new URL(link);
			URLConnection conn = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			PrintWriter pw = new PrintWriter(file);
			String line;
			while((line = br.readLine()) != null)
			{
				pw.println(line);
				if(line.startsWith("Title") && "".equals(title))
				{
					title = line.substring(line.indexOf(":") + 2);
				}
				else if(line.startsWith("Author") && "".equals(author))
				{
					author = line.substring(line.indexOf(":") + 2);
				}
				else if(line.startsWith("Release") && "".equals(publishDate))
				{
					 int start = line.indexOf(":") + 2;
				     int end = line.indexOf("[") - 1;
				     publishDate = line.substring(start, end);
				}
				else if(line.startsWith("Language") && "".equals(language))
				{
					language = line.substring(line.indexOf(":") + 2);
				}
			}
			br.close();
			pw.close();
			
			fileContent = new byte[(int) file.length()];
			FileInputStream fis = new FileInputStream(file);
			fis.read(fileContent);
			fis.close();
		} 
		catch (IOException e) 
		{
			LoggerUtil.logException("Greska u preuzimanju knjige", e);
			e.printStackTrace();
		}
		ImageService imageService = new ImageService();
		String imageUrl = link.replace(".txt", ".cover.medium.jpg");
		System.out.println(imageUrl);
		
		Image image = imageService.downloadImage(imageUrl);
		Book book = new Book(title, id, author, publishDate, language, image);
		book.setImageLink(imageUrl);
		book.setFileContent(fileContent);
		return book;
	}
}
