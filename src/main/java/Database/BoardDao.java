package Database;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import EJBs.Board;
import EJBs.Lists;
import EJBs.User;
import Message.JMSClient;

@Stateless
public class BoardDao {

	@PersistenceContext(unitName = "hello")
	private EntityManager entityManager;
	
	@EJB
	private UserDao userDao;
	
	@EJB
	private ListsDao listDao;
	
	@Inject
	JMSClient jmsUtil;
	
	public String createBoard(Board board)
	{
		Board boardFound = findBoard(board.getBoardName());
		if(boardFound == null)
		{
			Board newBoard = new Board(board);
			try {
			entityManager.persist(newBoard);
			}catch(NoResultException e)
			{
				return e.toString();
			}
			jmsUtil.sendMessage("Board created with name: " + board.getBoardName());
			return "Board Created!";
		}
		else
		{
			return "Board with same name already exists!";
		}
	}
	
	
	public Board findBoard(String boardName)
	{
	        Query query = entityManager.createQuery("SELECT b FROM Board b WHERE b.boardName = :boardName");
	        query.setParameter("boardName", boardName);
	        	 try {
	        	        return (Board) query.getSingleResult();
	        	    } catch (NoResultException e) {
	        	    	System.out.println(e.toString());
	        	        return null; // No board found with the given name
	        	    }
	}
	
	public List<Board> getAllBoards() {
		    Query query = entityManager.createQuery("SELECT b.boardName FROM Board b");
		    List<Board> boardNames = query.getResultList();
		    return boardNames;
		
    }
	
	public String inviteCollaborator(String boardName, String collaboratorEmail) {
		Board boardFound = findBoard(boardName);
		if(boardFound == null)
		{
			return "Board Doesn't Exist!";
		}
		User collaboratorFound = new User();
		collaboratorFound = userDao.findUser(collaboratorEmail);		
		if(collaboratorFound == null)
		{
			return "User with this Email Doesn't Exist!";
		}
		boardFound.getCollaborators().add(collaboratorFound);
        entityManager.merge(boardFound);
        jmsUtil.sendMessage("User with email: "+ collaboratorFound.getEmail() + " has been invited to Board: "+ boardFound.getBoardName());
        return "Invite Sent!";
    }
	
	public List<User> getCollaborators(String boardName)
	{
	    Query query = entityManager.createQuery("SELECT b.collaborators FROM Board b WHERE b.boardName = :boardName");
	    query.setParameter("boardName", boardName);
	    List<User> collaborators = query.getResultList();
	    return collaborators;
	}
	
	
	public String deleteBoard(Board board)
	{
		Board boardFound =  findBoard(board.getBoardName());
		if(boardFound == null)
		{			
			return "Board Doesn't Exist";
		}
		if(!boardFound.getLists().isEmpty()) {
			
			return "Please delete all lists first before deleting the board!";
		}
		
		 jmsUtil.sendMessage("Board with name: "+ board.getBoardName() + " deleted!");
		entityManager.remove(entityManager.merge(boardFound));
		return "Board Removed Successfully";
	}
	
	
	
}
