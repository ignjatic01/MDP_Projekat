package net.etf.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.*;

public class LoggerUtil 
{
    private static final Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());
    private static final String PATH;
    
    static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) 
		{
            prop.load(input);
            PATH = prop.getProperty("LOGGING_PATH");
        } 
		catch (IOException e) 
		{
            e.printStackTrace();
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}

    static 
    {
        try 
        {
            FileHandler fileHandler = new FileHandler(PATH + File.separator + "server.log", true);
            fileHandler.setFormatter(new SimpleFormatter()); // Format logova
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public static void logException(String message, Exception e) 
    {
        LOGGER.log(Level.SEVERE, message, e);
    }
}

