package com.example.todolist.model;

/**
 * A POJO representing a todo item in the list
 * @author Jake Shamash
 */
public class TodoItem {
	private String id;
	private String title;
	private String body;
	private boolean done;
	
	public TodoItem(String title, String body, boolean done) {
		super();
		this.id = "";
		this.title = title;
		this.body = body;
		this.done = done;
	}
	public TodoItem(String id, String title, String body, boolean done) {
		super();
		this.id = id;
		this.title = title;
		this.body = body;
		this.done = done;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @return true if it has been completed, false otherwise
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * @param done the done to set
	 */
	public void setDone(boolean done) {
		this.done = done;
	}	
	
	/**
	 * Toggle between done/not done
	 */
	public void toggleDone() {
		done = !done;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
}
