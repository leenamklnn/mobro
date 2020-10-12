package makelainen.mobro

//TODO: Make some of the fields optional and handle numeric fields as Int
case class Movie(name: String,
                 year: String,
                 genres: String, //TODO: Support List of Strings
                 ageLimit: String,
                 rating: String,
                 //TODO: actors: List[String],
                 director: String,
                 synopsis: String)

//TODO: Implement name as separated first and last name
case class FullName(firstName: String,
                    lastName: String)

case class RemoveMovieRequest(name: String)

case class MoviesResponse(payload: Seq[Movie])
