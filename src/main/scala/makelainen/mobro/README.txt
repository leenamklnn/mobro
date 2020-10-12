This is the first release of the Scala backend for MoBro - The Greatest Movie Browser Ever.

Run Bootstrap to start the backed HttpServer.
The server is configured to localhost:8089 by default.

The API:

/mobro/movies
GET request will return the current movies as json

/mobro/add
POST request will add a new movie to the data source
Example request:
{
  "name": "Working hard",
  "year": "2020",
  "genres": "Action",
  "ageLimit": "18",
  "rating": "5",
  "director": "Leena Mäkeläinen",
  "synopsis": "Leena is coding with Scala"
}

/mobro/remove
POST request will remove the movie with the defined name from the data source
Example request:
{
  "name": "Avengers: Endgame"
}

Next version will have the following improvements:
- Handling movies in a database instead of a file
- Support listing actors for the movies
- Sorting movies
- Searching movies
- Handle data types (all Strings now) and validate values in REST calls
