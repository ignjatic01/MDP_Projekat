package net.etf.api;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.etf.model.User;
import net.etf.service.UserService;

@Path("/users")
public class APIUser 
{
	UserService service;
	
	public APIUser()
	{
		service = new UserService();
	}
	
	@GET
	@Path("/notallowed")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<User> getNotAllowedUsers()
	{
		return service.getNotAllowedUsers();
	}
	
	@GET
	@Path("/allowed")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<User> getAllowedUsers()
	{
		return service.getAllowedUsers();
	}
	
	@GET
	@Path("/email/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserEmail(@PathParam("user") String user)
	{
		return service.getUserEmail(user);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(User user)
	{
		if(service.login(user.getUsername(), user.getPassword())) 
		{
			return Response.status(200).entity("OK").build();
		}
		else
		{
			return Response.status(401).entity("NOK").build();
		}
	}
	
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response register(User user)
	{
		if(user.getName() != null && user.getSurname() != null && user.getAddress() != null && user.getEmail() != null &&
				user.getUsername() != null && user.getPassword() != null)
		{
			if(service.register(user))
			{
				return Response.status(202).entity("ACCEPTED").build();
			}
			else
			{
				return Response.status(409).entity("USERNAME_EXISTS").build();
			}
		}
		return Response.status(400).entity("BAD_REQUEST").build();
	}
	
	@PUT
	@Path("/allow/{user}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response allowUser(@PathParam("user") String username)
	{
		if(service.allowUser(username))
		{
			return Response.status(200).entity("ALLOWED").build();
		}
		else
		{
			return Response.status(404).entity("USER_NOT_FOUND").build();
		}
	}
	
	@PUT
	@Path("/block/{user}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response blockUser(@PathParam("user") String username)
	{
		if(service.blockUser(username, true))
		{
			return Response.status(200).entity("BLOCKED").build();
		}
		else
		{
			return Response.status(404).entity("USER_NOT_FOUND").build();
		}
	}
	
	@PUT
	@Path("/unblock/{user}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response unblockUser(@PathParam("user") String username)
	{
		if(service.blockUser(username, false))
		{
			return Response.status(200).entity("UNBLOCKED").build();
		}
		else
		{
			return Response.status(404).entity("USER_NOT_FOUND").build();
		}
	}
	
	@DELETE
	@Path("/{user}")
	public Response delete(@PathParam("user") String username)
	{
		if(service.deleteUser(username))
		{
			return Response.status(200).entity("DELETED").build();
		}
		else
		{
			return Response.status(404).entity("USER_NOT_FOUND").build();
		}
	}
}
