package net.etf.api;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.etf.model.Book;
import net.etf.service.BookService;

@Path("/book")
public class APIBook 
{
	BookService service;
	
	public APIBook()
	{
		service = new BookService();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Book> getBooks()
	{
		return service.getBooks();
	}
	
	@GET
	@Path("/nocontent")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Book> getBooksNoContent()
	{
		return service.getBooksNoContent();
	}
	
	@GET
	@Path("/firstlines/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<String> getFirst100(@PathParam("id") long id)
	{
		return service.getFirst100(id);
	}
	
	@GET
	@Path("/download/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Book downloadBook(@PathParam("id") long id)
	{
		return service.getBook(id);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addOrUpdateBook(Book book)
	{
		if(service.addOrUpdateBook(book))
		{
			return Response.status(200).entity("OK").build();
		}
		else
		{
			return Response.status(500).entity("NOK").build();
		}
	}
	
	/*@POST
	@Path("/list")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addOrUpdateBookList(ArrayList<Book> books)
	{
		if(service.addOrUpdateBookList(books))
		{
			return Response.status(200).entity("OK").build();
		}
		else
		{
			return Response.status(500).entity("NOK").build();
		}
	}*/
	
	@DELETE
	@Path("/{id}")
	public Response delete(@PathParam("id") long id)
	{
		if(service.deleteBook(id))
		{
			return Response.status(200).build();
		}
		return Response.status(404).build();
	}
}
