package service;

import java.util.List;

import com.example.todolist.model.TodoItem;

/**
 * Interface for classes that read from a datastore. The purpose of this
 * interface is to allow replacement of the database with another database
 * provider or an in-memory datastore.
 * 
 * @author Jake Shamash
 * 
 */
public interface DataReadingService {

	/**
	 * Get an item by id
	 * @param id
	 * @return The item
	 */
	public abstract TodoItem getItem(String id);

	/**
	 * Gets all items from the collection
	 * @return a list of the items
	 */
	public abstract List<TodoItem> getItems();

}