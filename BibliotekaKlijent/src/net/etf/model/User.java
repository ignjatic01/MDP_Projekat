package net.etf.model;

import java.util.Objects;

public class User 
{
	private String name;
	private String surname;
	private String address;
	private String email;
	private String username;
	private String password;
	private boolean alowed;
	private boolean blocked;
		
	public User() 
	{
		super();
		alowed = false;
	}

	public User(String name, String surname, String address, String email, String username, String password) 
	{
		super();
		this.name = name;
		this.surname = surname;
		this.address = address;
		this.email = email;
		this.username = username;
		this.password = password;
		this.alowed = false;
		this.blocked = false;
	}
	
	public User(String username)
	{
		this.username = username;
		this.alowed = false;
	}
	
	public User(String username, String password)
	{
		this.username = username;
		this.password = password;
		this.alowed = false;
	}

	public String getName() 
	{
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}

	public String getSurname() 
	{
		return surname;
	}

	public void setSurname(String surname) 
	{
		this.surname = surname;
	}

	public String getAddress() 
	{
		return address;
	}

	public void setAddress(String address) 
	{
		this.address = address;
	}

	public String getEmail() 
	{
		return email;
	}

	public void setEmail(String email) 
	{
		this.email = email;
	}

	public String getUsername() 
	{
		return username;
	}

	public void setUsername(String username) 
	{
		this.username = username;
	}

	public String getPassword() 
	{
		return password;
	}

	public void setPassword(String password) 
	{
		this.password = password;
	}
	
	public boolean isAlowed() 
	{
		return alowed;
	}

	public void setAlowed(boolean alowed) 
	{
		this.alowed = alowed;
	}

	@Override
	public int hashCode() 
	{
		return Objects.hash(username);
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(username, other.username);
	}

	@Override
	public String toString() 
	{
		return "User [name=" + name + ", surname=" + surname + ", address=" + address + ", email=" + email
				+ ", username=" + username + "]";
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}
	
	
	
}
