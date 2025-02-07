package net.etf.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Book implements Serializable
{
	private String title;
	private long id;
	private String author;
	private String publishDate;
	private String language;
	private Image firstPage;
	private byte[] fileContent;
	private int count;
	private String imageLink;
	
	public Book() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Book(String title, long id, String author, String publishDate, String language, Image firstPage) {
		super();
		this.title = title;
		this.id = id;
		this.author = author;
		this.publishDate = publishDate;
		this.language = language;
		this.firstPage = firstPage;
	}
	
	public Book(String title, long id, String author, String publishDate, String language, Image firstPage, int count) {
		super();
		this.title = title;
		this.id = id;
		this.author = author;
		this.publishDate = publishDate;
		this.language = language;
		this.firstPage = firstPage;
		this.count = count;
	}
	
	

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(String publishDate) {
		this.publishDate = publishDate;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Image getFirstPage() {
		return firstPage;
	}

	public void setFirstPage(Image firstPage) {
		this.firstPage = firstPage;
	}

	public String getImageLink() {
		return imageLink;
	}

	public void setImageLink(String imageLink) {
		this.imageLink = imageLink;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Book other = (Book) obj;
		return id == other.id;
	}
	
	
}
