# Todo List Application

A simple todo list API.
Hosted at http://jake-todo-list.herokuapp.com/

## Notes
This application exposes an API for creating/querying todo items and stores items in a MongoDB server hosted on MongoHQ.
When an item is marked done, a text message is sent to the phone number specified by DEST_NUMBER in the source file com.example.todolist.resource.TodoResource

There are, however, some features of this application that are incomplete due to time constraints:

* There are no unit tests
* There is no simple UI for making API calls, testing must be done instead using curl or some other client.
* The addition of PUT requests to /todo/:id/:fieldname that update only that fieldname of the specified item (as now all fields must be specified to update an item).

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
	"_id" : "537694036451eb57f491a9e5"
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
