package Database;

import java.util.ArrayList;
import java.util.Iterator;
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
public class ListsDao {
	
	@PersistenceContext(unitName = "hello")
	private EntityManager entityManager;
	
	@EJB
	private BoardDao boardDao;
	
	@Inject
	JMSClient jmsUtil;

	public String createList(String board, String list)
	{
		try
		{		
			Board boardFound = new Board();
			Lists newList = new Lists();
			if(findList(board, list) != null)
			{
				return "List with same name already exists";
			}
			newList.setListName(list);
			boardFound = boardDao.findBoard(board);
			if(boardFound == null)
			{
				return "Board Doesn't Exist";
			}
			newList.setBoard(boardFound);
			entityManager.persist(newList);
			boardFound.getLists().add(newList);
			entityManager.merge(boardFound);
			jmsUtil.sendMessage("List Created with name " + newList.getListName() + " in Board: " + boardFound.getBoardName());
		}catch(Exception e)
		{
			return e.toString();
		}
		return "List Added";
	}
	
	public List<Lists> getList(String boardName)
	{
		try
		{
			Query query = entityManager.createQuery("SELECT l.listName FROM Lists l WHERE l.board.boardName = :boardName");
			query.setParameter("boardName" , boardName);
			List<Lists> lists = query.getResultList();
			return lists;
		}catch(Exception e)
		{
			System.out.println(e.toString());
			return null;
		}
		
	}
	
	public String deleteList(String boardName, String listName)
	{
		Lists listFound = findList(boardName, listName);
		Board boardFound =  boardDao.findBoard(boardName);
		if(boardFound == null)
		{			
			return "Board Doesn't Exist";
		}
		else if(listFound == null)
		{
			return "List Doesn't Exist";
		}
		try {
			if(!listFound.getCards().isEmpty())
			{
				Iterator<Card> iterator = listFound.getCards().iterator();
				while (iterator.hasNext()) {
					Card card = iterator.next();
					iterator.remove(); // Remove the current card from the list
					entityManager.remove(entityManager.merge(card)); // Remove the card from the database
				}
			}
			boardFound.getLists().remove(listFound);
			entityManager.remove(entityManager.merge(listFound));
			jmsUtil.sendMessage("List: " + listFound.getListName() + " removed from Board!");
	        return "List Removed!";
	    } catch (Exception e) {
	        return e.toString();
	    }
		
	}
	
	public Lists findList(String boardName, String listName)
	{
		Query query = entityManager.createQuery("SELECT l FROM Lists l WHERE l.board.boardName = :boardName AND l.listName = :listName");
		    query.setParameter("boardName", boardName);
		    query.setParameter("listName", listName);
		    try {
		    	Lists listFound = (Lists) query.getSingleResult();
		        return listFound;
		    } catch (NoResultException e) {
		        return null;
		    }
	}

	
	
	
	
}
