package com.example.todolist.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.example.todolist.model.TodoItem;
import com.mongodb.MongoException;
import com.owlike.genson.stream.JsonReader;
//import com.owlike.genson.stream.JsonReader;
//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.WebResource;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

import datastore.MongoService;

@Path("todo")
public class TodoResource {
	
	// TODO move to config file
	private static final String ACCOUNT_SID = "AC013d2d273f8193d60cb7654672f5aab0";
	private static final String AUTH_TOKEN = "fd40f8ee74bd6ec026bead17925d5c5d";
	private static final String DEST_NUMBER = "+15148652279";
	private static final String SRC_NUMBER = "+14387938511";
	private static final String SEARCHLY_ENDPOINT =
			"https://todolist:bdcsprj9vle4qmtlauzvu4ozduu4sptf@dwalin-us-east-1.searchly.com/todolist/_search";
	private static final String ES_JSON =
			"{ \"query\": { \"bool\": { \"should\": [ {\"match\": { \"title\":" +
			"{ \"query\": $query, \"boost\": 2 } }}, { \"match\": {\"body\" : $query}}" +
			"]}}}";
	
	/**
	 * Method handling GET request with specified ID. Returns the item with this id.
	 * @param id The ID of the item to query
	 * @return JSON representation of the item being queried.
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public TodoItem getItemById(@PathParam("id") String id) {
		TodoItem item;
		try {
			item = MongoService.getInstance().getItem(id);
			// Jersey will return a 204 Not Found if item is null.
			return item;
		} catch (MongoException e) {
			// Could not reach Mongo server
			throw new WebApplicationException(Status.BAD_GATEWAY);
		}
	}
	
	/**
	 * Method handling GET request. Returns all items in the database.
	 * @return JSON representation of all items in the database
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<TodoItem> getAllItems() {
		return MongoService.getInstance().getItems();
	}
	
	/**
	 * Method handling Http POST requests. Saves the item in the database.
	 * @param uriInfo URI info for the request
	 * @param item The item attached to the request (as JSON, converted to TodoItem)
	 * @return 201 Created if the insertion was successful.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveItem(@Context UriInfo uriInfo, TodoItem item) {
		try {
			String id = MongoService.getInstance().insertItem(item);
			// URI of the created item
			URI location = uriInfo.getBaseUriBuilder().path("todo/" + id).build();
			return Response.created(location).build();
		} catch (MongoException ex) {
			// Could not reach Mongo server
			throw new WebApplicationException(Status.BAD_GATEWAY);
		}
	}
	
	/**
	 * Delete an item by ID. Note: no special response if the item doesn't
	 * exist, since DELETE is idempotent
	 * 
	 * @param id The item ID
	 * @return 204 No Content
	 */
	@DELETE
	@Path("{id}")
	public Response deleteItemByID(@PathParam("id") String id) {
		MongoService.getInstance().deleteItem(id);
		return Response.noContent().build();
	}
	
	/**
	 * Delete all items
	 * 
	 * @return 204 No Content
	 */
	@DELETE
	public Response deleteAll() {
		MongoService.getInstance().deleteItems();
		return Response.noContent().build();
	}
	
	/**
	 * Updates an item given its id
	 * @param uriInfo
	 * @param id The path parameter specifying the ID
	 * @param item The JSON item
	 * @return See Other if successful, Not Modified if no such item.
	 */
	@POST
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateItem(@Context UriInfo uriInfo, @PathParam("id") String id, TodoItem item) {
		try {
			if (MongoService.getInstance().updateItem(id, item)) {
				// URI of the updated item
				URI location = uriInfo.getBaseUriBuilder().path("todo/" + id).build();
				// Best practice is to use a "See Other" response code, to tell
				// the client "I have performed your POST and the effect was that
				// some other resource was updated. See Location header for which
				// resource that was."
				return Response.seeOther(location).build();
			}
			else {
				// Nothing was updated.
				return Response.notModified().build();
			}
		} catch (MongoException ex) {
			// Could not reach Mongo server
			throw new WebApplicationException(Status.BAD_GATEWAY);
		}
	}
	
	@PUT
	@Path("{id}/toggledone")
	public Response toggleDone(@PathParam("id") String id) {
		// Get the item
		TodoItem item = MongoService.getInstance().getItem(id);
		if (item == null) {
			throw new WebApplicationException(404);
		}
		// Update the item
		item.toggleDone();
		MongoService.getInstance().updateItem(id, item);
		
		// If the item is done, send the user a text
		if (item.isDone()) {
			 TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
			    
		    // Build a filter for the MessageList
		    List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("Body", "Item " + item.getTitle() + " has been marked done."));
		    params.add(new BasicNameValuePair("To", DEST_NUMBER));
		    params.add(new BasicNameValuePair("From", SRC_NUMBER));	     
		     
		    try {
			    MessageFactory messageFactory = client.getAccount().getMessageFactory();
			    Message message = messageFactory.create(params);
		    } catch (TwilioRestException ex) {
		    	// Handle silently -- the message won't send, but we won't report an error.
		    }
		}
		
		// The item has been updated, respond with no content
		return Response.noContent().build();
	}
	
	/**
	 * {endpoint}/todo/search?q={keyword}
	 * This should search for items containg keyword. Not working due to issues with the Jersey client dependency.
	 * @param query The keyword
	 * @return The list of items found in order of relevance (null, for now).
	 */
	@Path("search")
	@GET
	public List<TodoItem> search(@QueryParam("q") String query) {
//		Client client = Client.create();
//		WebResource res = client.resource(SEARCHLY_ENDPOINT);
//		// Do a find and replace in the ElasticSearch query string
//		String json = ES_JSON.replaceAll("\\$query", query);
//		ClientResponse response = res.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, json);
//		List<TodoItem> items = readItems(new JsonReader(response.getEntity(String.class)));
//		return items;
		return null;
	}
	
	/**
	 * A method for parsing the JSON returned by a Searchly request into a list of items.
	 * 
	 * @param reader A JsonReader for the JSON
	 * @return A list of items found.
	 */
	//Ugly, but arguably a nicer implementation than creating POJOs for each nested object in the JSON.
	private static List<TodoItem> readItems(JsonReader reader) {
		LinkedList<TodoItem> items = new LinkedList<TodoItem>();
		while(reader.hasNext()) {
			String name = reader.next().name();
			if (name.contains("hits")) {
				// Outer "hits" object
				while(reader.hasNext()) {
					name = reader.next().name();
					if (name.contains("hits")) {
						// Inner "hits" object
						while (reader.hasNext()) {
							String body = "", title = "", id = "";
							boolean done = false;
							name = reader.next().name();
							if (name.contains("id")) id = reader.next().name();
							else if (name.contains("body")) body = reader.next().name();
							else if (name.contains("title")) title = reader.next().name();
							else if (name.contains("done")) done = Boolean.parseBoolean(reader.next().name());
							items.add(new TodoItem(id, title, body, done));
						}
					}
				}
			}
		}
		return items;
	}
	
}
