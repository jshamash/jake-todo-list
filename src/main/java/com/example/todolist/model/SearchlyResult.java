package com.example.todolist.model;

/**
 * Main body of the Searchly JSON response
 * @author Jake Shamash
 */
public class SearchlyResult {
	SearchlyHits hits;

	public SearchlyResult(SearchlyHits hits) {
		this.hits = hits;
	}
	
	public SearchlyHits getSearchlyHits() {
		return hits;
	}
}
