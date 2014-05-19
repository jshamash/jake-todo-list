package service;

import com.example.todolist.model.TodoItem;
import com.mongodb.MongoException;

/**
 * Interface for classes that write to a datastore. The purpose of this
 * interface is to allow replacement of the database with another database
 * provider or an in-memory datastore.
 * 
 * @author Jake Shamash
 * 
 */
public interface DataWritingService {

	/**
	 * Insert an item into the collection
	 * 
	 * @param item
	 * @return The id of the newly inserted item
	 */
	public abstract String insertItem(TodoItem item);

	/**
	 * Deletes an item by ID
	 * 
	 * @param id
	 */
	public abstract void deleteItem(String id);

	/**
	 * Deletes all items in the collection
	 */
	public abstract void deleteItems();

	/**
	 * Update item with id to newItem
	 * 
	 * @param id
	 * @param newItem
	 * @return true if the item exists, false otherwise.
	 */
	public abstract boolean updateItem(String id, TodoItem newItem)
			throws MongoException;

}