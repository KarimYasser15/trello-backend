package Database;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import EJBs.User;

@Stateless
public class UserDao {
	
	@PersistenceContext(unitName = "hello")
	private EntityManager entityManager;
	
	private User userLogin;
	
	public String registerUser(User user)
	{
		User userExists = findUser(user.getEmail());
		if(userExists == null)
		{
			if(!checkEmail(user.getEmail()))
			{
				return "Email isn't valid!";
			}
			if(!checkPassword(user.getPassword()))
			{
				return "Password is weak!";
			}
			entityManager.persist(user);
			return "User Registered Successfully";
		}
		else
		{
			return "User Already Exists";
		}
	}

	public User findUser(String email) {
        Query query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email");
        query.setParameter("email", email);
        try {
            return (User) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

	
	public String updateUser(User user)
	{
		if(userLogin != null)
		{
			if(user.getName() != null)
			{
				userLogin.setName(user.getName());
			}
			if(user.getEmail() != null)
			{
				if(checkEmail(user.getEmail()))
				{
					userLogin.setEmail(user.getEmail());
				}
				else 
					{
					return "Email isn't valid";
					}
			}
			if(user.getPassword() != null)
			{
				if(checkPassword(user.getPassword()))
				{					
					userLogin.setPassword(user.getPassword());
				}
				else
				{					
					return "Password is weak";
				}
			}
			entityManager.merge(userLogin);
			return "User Updated Successfully!";
		}
		else
		{
			return "User isn't logged in!";
		}
		
	}

	public String loginUser(User user)
		{
			User userExists = findUser(user.getEmail());
			if(userExists != null && userExists.getPassword().equals(user.getPassword()))
			{
				userLogin = userExists;
				return "Login Success!";
			}
			else if(userExists != null && !userExists.getPassword().equals(user.getPassword()))
			{
				return "Password Incorrect!";
			}
			else
			{
				return "User Doesn't Exist!";
			}
				
		}
	
	public Boolean checkEmail(String email)
	{
		if(!email.contains("@") || !email.contains(".com"))
		{
			return false;
		}
		return true;
	}
	
	public Boolean checkPassword(String password)
	{
		if(password.length() < 8)
		{	
			return false;
		}
		return true;
	}
		
		

}
