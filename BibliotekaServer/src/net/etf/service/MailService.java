package net.etf.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.etf.model.Book;

public class MailService 
{
	public static final String PATH;
	public static final String MAIL_CONF_PATH;
	public static final String ZIP_PATH;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(System.getProperty("user.dir") + File.separator + "src" + File.separator + "resources/config.properties")) {
            prop.load(input);
            PATH = prop.getProperty("MAIN_PATH");
            MAIL_CONF_PATH = prop.getProperty("MAIL_CONF_PATH");
            ZIP_PATH = prop.getProperty("ZIP_PATH");
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
	private String username;
	private String password;
	
	private Properties loadMailConfig() throws FileNotFoundException, IOException 
	{
	    Properties serverprop = new Properties();

	    Properties mailProp = new Properties();
	    mailProp.load(new FileInputStream(new File(System.getProperty("user.dir") + MAIL_CONF_PATH)));

	    username = mailProp.getProperty("username");
	    password = mailProp.getProperty("password");
	   
	    return mailProp;
	}

	public boolean sendMail(String to, String title, String body, ArrayList<Book> books) throws FileNotFoundException, IOException 
	{
	    Properties props = loadMailConfig();

	    Session session = Session.getInstance(props, new javax.mail.Authenticator() {
	        protected PasswordAuthentication getPasswordAuthentication() {
	            return new PasswordAuthentication(username, password);
	        }
	    });
	    
	    try {
	        Message message = new MimeMessage(session);
	        message.setFrom(new InternetAddress(username));
	        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
	        message.setSubject(title);
	        message.setText(body);
	        
	        BookService bs = new BookService();
	        ArrayList<Book> allBooks = bs.getBooks();
	        ArrayList<Book> booksForZip = new ArrayList<Book>();
	        
	        for(Book b : books)
	        {
	        	for(Book ba : allBooks)
	        	{
	        		if(b.getId() == ba.getId())
	        		{
	        			booksForZip.add(ba);
	        		}
	        	}
	        }
	        
	        StringBuilder sb = new StringBuilder();
	        sb.append("Narucene knjige: \n");
	        for(Book b : booksForZip)
	        {
	        	System.out.println(b.getFileContent().length);
	        	sb.append(b.getTitle() + "\n");
	        }
	        
	        zipBooks(booksForZip, System.getProperty("user.dir") + ZIP_PATH + "books.zip");
	        
	        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
	        attachmentBodyPart.attachFile(System.getProperty("user.dir") + ZIP_PATH + "books.zip");
	        
	        MimeBodyPart textBodyPart = new MimeBodyPart();
	        textBodyPart.setText(sb.toString(), "utf-8");
	        //attachmentBodyPart.attachFile("Prilog.txt");
	        
	        Multipart multipart = new MimeMultipart();
	        multipart.addBodyPart(textBodyPart);
	        multipart.addBodyPart(attachmentBodyPart);
	        
	        message.setContent(multipart);
	        
	        Transport.send(message);
	        return true;
	    } catch (MessagingException e) {
	    	LoggerUtil.logException("Greska u slanju mejla", e);
	        e.printStackTrace();
	        return false;
	    }
	}
	
	/*public static void zipBooks(ArrayList<Book> books, String zipFilePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            
            for (Book book : books) {
                if (book.getFileContent() != null && book.getFileContent().length > 0) {
                    // Ime fajla u zip arhivi
                    String fileName = book.getTitle().replaceAll("\\s+", "_") + ".txt"; 

                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zipOut.putNextEntry(zipEntry);
                    zipOut.write(book.getFileContent());
                    zipOut.closeEntry();
                }
            }
        }
    }*/
	
	public static void zipBooks(ArrayList<Book> books, String zipFilePath) throws IOException {
	    File zipFile = new File(zipFilePath);
	    File parentDir = zipFile.getParentFile(); // Dohvati folder gde će biti zip fajl

	    // Ako parent direktorijum ne postoji, kreiraj ga
	    if (parentDir != null && !parentDir.exists()) 
	    {
	        if (!parentDir.mkdirs()) 
	        {
	            throw new IOException("Could not create directory: " + parentDir.getAbsolutePath());
	        }
	    }

	    try (FileOutputStream fos = new FileOutputStream(zipFile);
	         ZipOutputStream zipOut = new ZipOutputStream(fos)) 
	    {
	        
	        for (Book book : books) {
	            if (book.getFileContent() != null && book.getFileContent().length > 0) {
	                String fileName = book.getTitle().replaceAll("\\s+", "_") + ".txt"; 

	                ZipEntry zipEntry = new ZipEntry(fileName);
	                zipOut.putNextEntry(zipEntry);
	                zipOut.write(book.getFileContent());
	                zipOut.closeEntry();
	            }
	        }
	    }
	}

}
