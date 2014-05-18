package com.example.todolist.model;

/**
 * An element of the Searchly JSON response
 * @author Jake Shamash
 */
public class SearchlyHit {
	TodoItem _source;

	public SearchlyHit(TodoItem _source) {
		this._source = _source;
	}

	public TodoItem get_source() {
		return _source;
	}
}