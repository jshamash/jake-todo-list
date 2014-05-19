package service;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

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

/**
 * A singleton class. Provides functionality for interacting with the Mongo database.
 * @author Jake
 *
 */
public class MongoService implements DataWritingService, DataReadingService {
	private static MongoService instance = null;
	private String collectionName;
	private final static Logger LOGGER = Logger.getLogger(MongoService.class.getName()); 
	DB db;
	DBCollection todoItems;
	
	private MongoService() {
		if (instance != null) {
			throw new IllegalStateException("Already instantiated!");
		}
		Properties properties = new Properties();
		try {
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));
			collectionName = properties.getProperty("mongo_collection_name");
		} catch (Exception e) {
			LOGGER.warning("No config.properties file found, using defaults");
			collectionName = "todoitems";
		}
		LOGGER.info("Set mongo collection name to " + collectionName);
		LOGGER.info("Connecting to Mongo Service...");
		connect();
	}
	
	public static MongoService getInstance() {
		if (instance == null)
			instance = new MongoService();
		return instance;
	}
	
	private void connect() {
    	try {
			MongoURI mongoURI = new MongoURI(System.getenv("MONGOHQ_URL"));
			db = mongoURI.connectDB();
			db.authenticate(mongoURI.getUsername(), mongoURI.getPassword());
			todoItems = db.getCollection(collectionName);
			LOGGER.info("Connected to Mongo Service");
		} catch (MongoException e) {
			// Could not authenticate -- 502 bad gateway (not client's fault)
			throw new WebApplicationException(Status.BAD_GATEWAY);
		} catch (UnknownHostException e) {
			// Could not determine ip of host -- 504 gateway timeout
			throw new WebApplicationException(Status.GATEWAY_TIMEOUT);
		}
	}
	

	/* (non-Javadoc)
	 * @see datastore.DatastoreService#insertItem(com.example.todolist.model.TodoItem)
	 */
	@Override
	public String insertItem(TodoItem item) throws MongoException {
		BasicDBObject doc = convert(item);
		todoItems.insert(doc);
		return doc.get("_id").toString();
	}
	
	/* (non-Javadoc)
	 * @see service.DataReadingService#getItem(java.lang.String)
	 */
	@Override
	public TodoItem getItem(String id) throws MongoException {
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(id));
		DBObject dbObj = todoItems.findOne(query);
		if (dbObj == null) return null;
		return convert(dbObj);
	}
	
	/* (non-Javadoc)
	 * @see service.DataReadingService#getItems()
	 */
	@Override
	public List<TodoItem> getItems() throws MongoException {
		List<TodoItem> items = new LinkedList<TodoItem>();
		DBCursor cursor = todoItems.find();
		while(cursor.hasNext()) {
			items.add(convert(cursor.next()));
		}
		cursor.close();
		return items;
	}
	
	/* (non-Javadoc)
	 * @see datastore.DatastoreService#deleteItem(java.lang.String)
	 */
	@Override
	public void deleteItem(String id) throws MongoException {
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(id));
		todoItems.remove(query);
	}
	
	/* (non-Javadoc)
	 * @see datastore.DatastoreService#deleteItems()
	 */
	@Override
	public void deleteItems() {
		todoItems.remove(new BasicDBObject());
	}
	
	/* (non-Javadoc)
	 * @see datastore.DatastoreService#updateItem(java.lang.String, com.example.todolist.model.TodoItem)
	 */
	@Override
	public boolean updateItem(String id, TodoItem newItem) throws MongoException {
		BasicDBObject newObject = convert(newItem);
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(id));
		WriteResult result = todoItems.update(query, newObject);
		return (result.getN() < 1) ? false : true;
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
