package net.etf.api;

import java.io.IOException;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.etf.model.Book;
import net.etf.service.MailService;

@Path("/mail")
public class APIMail 
{
	MailService service = new MailService();
	
	@POST
	@Path("/{email}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendMail(ArrayList<Book> books, @PathParam("email") String email)
	{
		try
		{
			if(service.sendMail(email, "Test poruka", "Narudzba", books))
			{
				return Response.status(200).entity("SENT").build();
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return Response.status(401).entity("ERR").build();
	}
}
