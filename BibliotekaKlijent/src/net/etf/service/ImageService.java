package net.etf.service;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.imageio.ImageIO;

import net.etf.model.Image;

public class ImageService 
{
	private static final String PATH;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            PATH = prop.getProperty("IMAGES_PATH");
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
	public Image downloadImage(String photoURL)
	{
		Image image = new Image();
		try
		{
			URL url = new URL(photoURL);
			BufferedImage img = ImageIO.read(url);
			String fileName = photoURL.substring(photoURL.lastIndexOf("/") + 1);
			String fileExtension = photoURL.substring(photoURL.lastIndexOf(".") + 1).trim();
			
			if("jpeg".equals(fileExtension)) 
			{
				fileExtension = "jpg";
			}
			
			File file = new File(PATH + File.separator + fileName);
			//file.createNewFile();
			//ImageIO.write(img, fileExtension, file);
			image = new Image(fileName, 0, fileExtension, "", img);
		}
		catch(MalformedURLException e)
		{
			LoggerUtil.logException("Greska u ucitavanju URL-a", e);
			e.printStackTrace();
		}
		catch(IOException e)
		{
			LoggerUtil.logException("Greska u preuzimanju slike", e);
			e.printStackTrace();
		}
		return image;
	}
}
