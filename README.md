# Todo List Application

A simple todo list API.
Hosted at http://jake-todo-list.herokuapp.com/

## Notes
This application exposes an API for creating/querying todo items and stores items in a MongoDB server hosted on MongoHQ.
When an item is marked done, a text message is sent to the phone number specified by DEST_NUMBER in the source file com.example.todolist.resource.TodoResource

There are, however, some features of this application that are incomplete due to time constraints:

* The search functionality does not work. This is due to the fact that I could not get the Jersey client to work because of dependency issues. Given more time and experimentation this could be resolved. The code for the search functionality is commented out in the TodoResource class.
* There are no unit tests
* There is no simple UI for making API calls, testing must be done instead using curl or some other client.
* New entries in the DB aren't pushed to Searchly as they're added. Instead, Searchly crawls the database weekly. As such search requests (if they were working) don't search freshly added data. This is simple to fix by adding to the Searchly index via an API call whenever a database entry is added/changed.

## API
Base url: http://jake-todo-list.herokuapp.com/

JSON description of a resource for POST requests:
```javascript
{
	"title" : "sample title",
	"body" : "sample body",
	"done" : false
}
```

JSON description of a resource as returned by GET requests:
```javascript
{
	"id" : "537694036451eb57f491a9e5"
	"title" : "sample title",
	"body" : "sample body",
	"done" : false
}
```

| Resource        | Description |
| ------------- |-------------|
| GET todo      | Returns all todo items in the database |
| GET todo/:id      | Gets the item with id `:id`  |
| POST todo | inserts the item, specified by the attached JSON      |
| DELETE todo/:id | Deletes the item with id `:id`  |
| DELETE todo     | Deletes all items in the list |
| POST todo/:id   | Updates the item with id `:id` to the one specified in the attached JSON |
| PUT todo/:id/toggledone | toggles whether or not item with id `:id` should be marked as "done". |
| GET todo/search?q=:query | returns a list of items containing `:query`; items with `:query` in their title are given higher weight. |