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
	//public static final String PATH = File.separator + "src" + File.separator + "resources" + File.separator + "photos";
	
	public static final String PATH;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(System.getProperty("user.dir") + File.separator + "src" + File.separator + "resources/config.properties")) {
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
			
			File file = new File(System.getProperty("user.dir") + PATH + File.separator + fileName);
			ImageIO.write(img, fileExtension, file);
			image = new Image(fileName, file.length(), fileExtension, file.getAbsolutePath(), img);
		}
		catch(MalformedURLException e)
		{
			LoggerUtil.logException("Greska u formiranju URL-a", e);
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
