package makelainen.mobro


case class Movie(name: String,
                 year: Int,
                 genres: List[String],
                 ageLimit: Int,
                 rating: Int,
                 //actors: List[String],
                 director: String,
                 synopsis: String)

//TODO: Implement name as separated first and last name
case class FullName(firstName: String,
                    lastName: String)

case class RemoveMovieRequest(name: String)

case class MoviesResponse(payload: Seq[Movie])
