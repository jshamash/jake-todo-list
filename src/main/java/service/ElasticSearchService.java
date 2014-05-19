package service;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.example.todolist.model.SearchlyHit;
import com.example.todolist.model.SearchlyResult;
import com.example.todolist.model.TodoItem;

/**
 * Service for communicating with the ElasticSearch service, Searchly.io. Please
 * note that JEST is a client for making ElasticSearch API calls, and including
 * it here would make for a nicer implementation. However, I chose not to use it
 * in order to show how Jersey client can be used to consume an API.
 * 
 * @author Jake Shamash
 * 
 */
public class ElasticSearchService implements DataWritingService {
	private static final String SEARCHLY_ENDPOINT =
			"http://dwalin-us-east-1.searchly.com/";
	private static final String INDEX_NAME = "todolist";
	private static final String INDEX_TYPE = "todoitem";
	private static final String SEARCHLY_USER = "todolist";
	private static final String SEARCHLY_PASS = "bdcsprj9vle4qmtlauzvu4ozduu4sptf";
	// String for elasticsearch query, where $query is to be replaced by the client's query.
	// TODO move to json file
	private static final String ES_JSON =
			"{ \"query\": { \"bool\": { \"should\": [ {\"match\": { \"title\":" +
			"{ \"query\": \"$query\", \"boost\": 2 } }}, { \"match\": {\"body\" : \"$query\"}}" +
			"], \"minimum_should_match\" : 1}}}";
	
	private static ElasticSearchService instance = null;
	
	private Client client;
	
	private ElasticSearchService() {
		if (instance != null) {
			throw new IllegalStateException("Already instantiated!");
		}
		connect();
	}
	
	public static ElasticSearchService getInstance() {
		if (instance == null)
			instance = new ElasticSearchService();
		return instance;
	}
	
	private void connect() {
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(SEARCHLY_USER, SEARCHLY_PASS);
		client = ClientBuilder.newClient();
		client.register(feature);
	}
	
	@Override
	public String insertItem(TodoItem item) {
		URI uri = UriBuilder.fromPath(SEARCHLY_ENDPOINT)
				.path(INDEX_NAME)
				.path(INDEX_TYPE)
				.path(item.get_id())
				.build();
		WebTarget target = client.target(uri);
		//Future<Response> future = 
		target.request().async().post(Entity.entity(item, MediaType.APPLICATION_JSON));
		
//		try {
//			Response response = future.get(5, TimeUnit.SECONDS);
//			// TODO parse response to ensure success
//		} catch (InterruptedException | ExecutionException | TimeoutException e) {
//			e.printStackTrace();
//		}

		return item.get_id();
	}

	@Override
	public void deleteItem(String id) {
		URI uri = UriBuilder.fromPath(SEARCHLY_ENDPOINT)
				.path(INDEX_NAME)
				.path(INDEX_TYPE)
				.path(id)
				.build();
		WebTarget target = client.target(uri);
		target.request().async().delete();
	}

	@Override
	public void deleteItems() {
		URI uri = UriBuilder.fromPath(SEARCHLY_ENDPOINT)
				.path(INDEX_NAME)
				.path(INDEX_TYPE)
				.build();
		WebTarget target = client.target(uri);
		target.request().async().delete();
	}

	@Override
	public boolean updateItem(String id, TodoItem newItem) {
		// Deletes the old item and re-adds the new item
		deleteItem(id);
		newItem.set_id(id);
		insertItem(newItem);
		return true;
	}
	
	public List<TodoItem> search(String query) throws InterruptedException, ExecutionException, TimeoutException {
		URI uri = UriBuilder.fromPath(SEARCHLY_ENDPOINT)
				.path(INDEX_NAME)
				.path("_search")
				.build();
		WebTarget target = client.target(uri);
		String json = ES_JSON.replaceAll("\\$query", query);
		Future<Response> future = target.request().async().post(Entity.entity(json, MediaType.APPLICATION_JSON));
		
		// Wait 5 seconds for future to complete
		Response response = future.get(5, TimeUnit.SECONDS);
		SearchlyResult result = response.readEntity(SearchlyResult.class);
		
		LinkedList<TodoItem> list = new LinkedList<TodoItem>();
		
		// Add all the search results to the list (they are in order of relevance)
		for (SearchlyHit hit : result.getSearchlyHits().getSearchlyHitList()) {
			list.add(hit.get_source());
		}
		
		return list;
	}
}
