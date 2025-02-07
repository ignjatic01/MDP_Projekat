package net.etf.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class Account implements Serializable
{
	private HashMap<String, Integer> books;
	private Date date;
	private double cijena;
	
	public Account() {
		super();
		books = new HashMap<String, Integer>();
		date = new Date();
		cijena = 0;
	}

	public Account(HashMap<String, Integer> books, Date date, double cijena) {
		super();
		this.books = books;
		this.date = date;
		this.cijena = cijena;
	}

	public HashMap<String, Integer> getBooks() {
		return books;
	}

	public void setBooks(HashMap<String, Integer> books) {
		this.books = books;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public double getCijena() {
		return cijena;
	}

	public void setCijena(double cijena) {
		this.cijena = cijena;
	}
	
	public void addBook(String key, Integer value)
	{
		books.put(key, value);
		cijena += (new Random().nextInt(30) + 10)*value;
	}
	
}
