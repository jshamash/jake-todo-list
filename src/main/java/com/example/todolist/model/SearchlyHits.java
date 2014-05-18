package com.example.todolist.model;

import java.util.List;

/**
 * An element of the Searchly JSON response
 * @author Jake Shamash
 */
public class SearchlyHits {
	List<SearchlyHit> hits;

	public SearchlyHits(List<SearchlyHit> hits) {
		this.hits = hits;
	}

	public List<SearchlyHit> getSearchlyHitList() {
		return hits;
	}
}