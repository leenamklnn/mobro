package makelainen.mobro

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
 * Trait including the RootJsonFormats for required requests
 */
trait MoviesJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val removeMovieRequestFormat: RootJsonFormat[RemoveMovieRequest] = jsonFormat1(RemoveMovieRequest)
  implicit val addMovieRequestFormat: RootJsonFormat[Movie] = jsonFormat7(Movie)
  implicit val fullNameFormat: RootJsonFormat[FullName] = jsonFormat2(FullName)

}