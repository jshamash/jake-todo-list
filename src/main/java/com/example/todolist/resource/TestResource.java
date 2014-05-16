package com.example.todolist.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.todolist.model.TestObject;

@Path("test")
public class TestResource {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TestObject test() {
		return new TestObject(100, "hello");
	}
}
