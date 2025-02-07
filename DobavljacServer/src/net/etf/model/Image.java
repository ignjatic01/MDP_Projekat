package net.etf.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import javax.imageio.ImageIO;

import net.etf.service.LoggerUtil;

public class Image implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private long size;
    private String fileExtension;
    private String path;
    private transient BufferedImage image; // Ovo polje nije serijalizovano
    private byte[] imageData; // Bajt niz za sliku

    public Image() {
        super();
    }

    public Image(String name, long size, String fileExtension, String path, BufferedImage image) {
        this.name = name;
        this.size = size;
        this.fileExtension = fileExtension;
        this.path = path;
        setImage(image);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public BufferedImage getImage() 
    {
        if (image == null && imageData != null) 
        {
            try 
            {
                ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                image = ImageIO.read(bais);
            } 
            catch (IOException e) 
            {
            	LoggerUtil.logException("Greska u ucitavanu slike", e);
                e.printStackTrace();
            }
        }
        return image;
    }

    public void setImage(BufferedImage image) 
    {
        this.image = image;
        if (image != null) 
        {
            try 
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                this.imageData = baos.toByteArray();
            } 
            catch (IOException e) 
            {
            	LoggerUtil.logException("Greska u postavljanju slike", e);
                e.printStackTrace();
            }
        }
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
