package datastore;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.bson.types.ObjectId;

import com.example.todolist.model.TodoItem;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;
import com.mongodb.WriteResult;

public class MongoService {
	private static MongoService instance = null;
	private static final String COLLECTION_NAME = "todoItems";
	DB db;
	DBCollection todoItems;
	
	private MongoService() {
		if (instance != null) {
			throw new IllegalStateException("Already instantiated!");
		}
		connect();
	}
	
	public static MongoService getInstance() {
		if (instance == null)
			instance = new MongoService();
		return instance;
	}
	
	private void connect() {
    	try {
    		System.out.println("Trying to connect to mongo....");
    		
			MongoURI mongoURI = new MongoURI(System.getenv("MONGOHQ_URL"));
			db = mongoURI.connectDB();
			db.authenticate(mongoURI.getUsername(), mongoURI.getPassword());
			todoItems = db.getCollection(COLLECTION_NAME);
			
			System.out.println("Connected to Mongo");
		} catch (MongoException e) {
			// Could not authenticate -- 502 bad gateway (not client's fault)
			throw new WebApplicationException(Status.BAD_GATEWAY);
		} catch (UnknownHostException e) {
			// Could not determine ip of host -- 504 gateway timeout
			throw new WebApplicationException(Status.GATEWAY_TIMEOUT);
		}
	}
	

	public String insertItem(TodoItem item) throws MongoException {
		BasicDBObject doc = convert(item);
		todoItems.insert(doc);
		return doc.get("_id").toString();
	}
	

	public TodoItem getItem(String id) throws MongoException {
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(id));
		DBObject dbObj = todoItems.findOne(query);
		if (dbObj == null) return null;
		return convert(dbObj);
	}
	
	public List<TodoItem> getItems() throws MongoException {
		List<TodoItem> items = new LinkedList<TodoItem>();
		DBCursor cursor = todoItems.find();
		while(cursor.hasNext()) {
			items.add(convert(cursor.next()));
		}
		cursor.close();
		return items;
	}
	
	public void deleteItem(String id) throws MongoException {
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(id));
		todoItems.remove(query);
	}
	
	public void deleteItems() {
		todoItems.remove(new BasicDBObject());
	}
	
	public boolean updateItem(String id, TodoItem newItem) throws MongoException {
		BasicDBObject newObject = convert(newItem);
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(id));
		WriteResult result = todoItems.update(query, newObject);
		return (result.getN() < 1) ? false : true;
	}
	
	/**
	 * Toggles the "done" field on the item with specified id.
	 * @param id The ID of the item
	 * @return The updated status of this item (true = done, false = not done)
	 * @throws MongoException
	 * @throws NoSuchElementException if the element doesn't exist
	 */
	public boolean toggleDone(String id) throws MongoException, NoSuchElementException {
		
		return false;
	}
	
	private static TodoItem convert(DBObject obj) {
		String id = obj.get("_id").toString();
		String title = obj.get("title").toString();
		String body = obj.get("body").toString();
		boolean done = Boolean.parseBoolean(obj.get("done").toString());
		return new TodoItem(id, title, body, done);
	}
	private static BasicDBObject convert(TodoItem item) {
		return new BasicDBObject("title", item.getTitle()).
				append("body", item.getBody()).
				append("done", item.isDone());
	}

	
}
