package API;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import Database.UserDao;
import EJBs.User;

@Stateless
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserService {
	
	User userLogin = null;

	@EJB
	private UserDao userDB;
	
	@POST
	@Path("/register")
	public String registerUser(User user)
	{
		return userDB.registerUser(user);
	}
		
		@GET
		@Path("/login")
		public String loginUser(User user)
		{
			return userDB.loginUser(user);
		}
		
		@PUT
		@Path("/update")
		public String updateUser(User user)
		{
			return userDB.updateUser(user);
		}
		
	
	
	
}
