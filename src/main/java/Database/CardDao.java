package Database;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import EJBs.Board;
import EJBs.Card;
import EJBs.Lists;
import EJBs.User;
import Message.JMSClient;

@Stateless
public class CardDao {
	
	@PersistenceContext(unitName = "hello")
	private EntityManager entityManager;
	
	@EJB
	private BoardDao boardDao;
	
	@EJB
	private ListsDao listsDao;
	
	@EJB
	private UserDao userDao;
	
	@Inject
	JMSClient jmsUtil;
	
	public String createCard(String boardName, String listName, Card card)
	{
		
		Board boardFound = boardDao.findBoard(boardName);
		Lists listFound = listsDao.findList(boardName, listName);
		if(boardFound == null || listFound == null)
		{
			return "Board or List Doesn't Exist!";
		}
		try
		{
			
			Card cardFound = findCard(boardName, listName, card.getCardName());
			if(cardFound != null)
			{
				return "Card With Same Name Already Exists";
			}
			cardFound = new Card(card);
			cardFound.setBoard(boardFound);
			cardFound.setList(listFound);
			entityManager.persist(cardFound);
			listFound.getCards().add(cardFound);
			entityManager.merge(listFound);
			cardFound = findCard(boardName, listName, card.getCardName());
			if(cardFound == null)
			{
				return "Card Not ADDED!";
			}
			jmsUtil.sendMessage("Card " + card.getCardName() + " added to list " + listFound.getListName() + " in board " + boardFound.getBoardName());
			return "Card Added!";
		}
		catch(Exception e)
		{
			return e.toString();
		}
		
	}
	
	
	public Card findCard(String boardName, String listName, String cardName)
	{
		Query query = entityManager.createQuery("Select c FROM Card c where c.board.boardName = :boardName AND c.list.listName = :listName AND c.cardName = :cardName");
	    query.setParameter("boardName", boardName);
	    query.setParameter("listName", listName);
	    query.setParameter("cardName" , cardName);
	    try {
	    	Card cardFound = (Card) query.getSingleResult();
	        return cardFound;
	    } catch (NoResultException e) {
	        return null;
	    }
		
	}
	
	public String moveCard(String boardName ,String fromListName, String toListName, String cardName)
	{
		Lists fromList = listsDao.findList(boardName, fromListName);
		Lists toList = listsDao.findList(boardName, toListName);
		Card cardFound = findCard(boardName, fromListName, cardName);
		if(fromList == null || toList == null || cardFound == null)
		{
			return "List or Card Not Found!";
		}
		cardFound.setList(toList);
		fromList.getCards().remove(cardFound);
		toList.getCards().add(cardFound);
		entityManager.merge(fromList);
	    entityManager.merge(toList);
		jmsUtil.sendMessage("Card " + cardFound.getCardName() + " moved to from list " + fromList.getListName() + " to list " + toList.getListName());
	    return "Card Moved to List: "+ toList.getListName();	
	}
	
	public List<Card> getCards(String boardName, String listName)
	{
	    try {
	    	Query query = entityManager.createQuery("Select c.cardName , c.description FROM Card c where c.board.boardName = :boardName AND c.list.listName = :listName");
	    	query.setParameter("boardName", boardName);
	    	query.setParameter("listName", listName);
	    	List<Card> cards = query.getResultList();
	    	return cards;
	    } catch (Exception e) {
			System.out.println(e.toString());
	    	return null;
	    }
	}
	
	public String assignUsers(String userEmail, String boardName, String listName, String cardName)
	{
		Board boardFound = boardDao.findBoard(boardName);
		User userFound = userDao.findUser(userEmail);
		Card cardFound = findCard(boardName, listName, cardName);
		if(!boardFound.getCollaborators().contains(userFound))
		{
			return "User isn't a collaborator in this board!";
		}
		if(cardFound.getAssignedUsers().contains(userFound))
		{
			return "User is already Assigned to this Card!";
		}
		try
		{
			cardFound.getAssignedUsers().add(userFound);
			entityManager.merge(cardFound);
			if(cardFound.getAssignedUsers().contains(userFound))
			{
				jmsUtil.sendMessage("User " + userFound.getName() + " has been assigned to Card " + cardFound.getCardName());
				return "User Added!";
			}
			return "User Not Added!";
		}catch(Exception e)
		{
			return e.toString();
		}
		
	}
	
	public List<User> getAssignedUsers(String boardName, String listName, String cardName)
	{
		try
		{
    	Query query = entityManager.createQuery("Select c.assignedUsers FROM Card c where c.board.boardName = :boardName AND c.list.listName = :listName AND c.cardName = :cardName");
    	query.setParameter("boardName", boardName);
    	query.setParameter("listName", listName);
    	query.setParameter("cardName", cardName);
    	List<User> assignedUsers = query.getResultList();
    	return assignedUsers;
		}catch(Exception e)
		{
			System.out.println(e.toString());
			return null;
		}
	}
	
	public String getComment(String boardName, String listName, String cardName)
	{
		try
		{
    	Query query = entityManager.createQuery("Select c.comment FROM Card c where c.board.boardName = :boardName AND c.list.listName = :listName AND c.cardName = :cardName");
    	query.setParameter("boardName", boardName);
    	query.setParameter("listName", listName);
    	query.setParameter("cardName", cardName);
    	String data =  (String) query.getSingleResult();
    	return data;
		}catch(Exception e)
		{
			System.out.println(e.toString());
			return null;
		}
		
	}
	
	public String postComment(String boardName, String listName, String cardName, String comment)
	{
		Card cardFound = findCard(boardName, listName, cardName);
		if(cardFound == null)
		{
			return "Card Not Found";
		}
		try
		{
			cardFound.setComments(comment);
			entityManager.merge(cardFound);
			jmsUtil.sendMessage(comment + "\n The above comment has been posted");
			return "Comment Added";
		}
		catch(Exception e)
		{
			return e.toString();
		}
	}
	
	
	
	
	
	
	
	
	
	
}
