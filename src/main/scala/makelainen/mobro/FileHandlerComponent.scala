package makelainen.mobro

import java.io.File
import java.nio.file.{Files, Paths}
import akka.Done
import com.fasterxml.jackson.core.`type`.TypeReference
import scala.concurrent.Future

/**
 * Cake pattern component for FileHandler
 */
trait FileHandlerComponent extends ObjectMapper {

  val fileHandler: FileHandler

  /**
   * FileHandler for reading and updating movies data source file
   */
  class FileHandler() {

    // TODO: Make the location configurable
    val filePath = "movies.json"

    /**
     * Reads the movies data source file and parses it to Movie objects
     *
     * @return List of Movie objects parsed from the file
     */
    def readMoviesFromFile(): List[Movie] = {
      println("Fetching movies")
      objectMapper.readValue(new File(filePath), new TypeReference[List[Movie]](){})
    }

    /**
     * Adds a new Movie object as json to the movies data source file
     *
     * @param newMovie Movie object to be added
     * @return Done, when the new movie has been added to the file
     */
    def appendMovieToFile(newMovie: Movie): Future[Done] = {
      println("Adding a movie")
      val path = Paths.get(filePath)
      if (!Files.exists(path)) {
        println("Creating " + path)
        Files.createFile(path)
        objectMapper.writeValue(new File(filePath), List(newMovie))
      }
      else {
        val movieList = readMoviesFromFile() :+ newMovie
        objectMapper.writeValue(new File(filePath), movieList)
      }
      Future.successful(Done)
    }

    /**
     * Removes a movie with the defined name from the movies data source file
     *
     * @param movieName Name of the movie to be removed
     * @return Done, when the movie has been removed
     */
    def removeMovieFromFile(movieName: String): Future[Done] = {
      println("Removing a movie")
      val movieList = readMoviesFromFile().filterNot(_.name == movieName)
      println("Movielist: " + movieList)
      objectMapper.writeValue(new File(filePath), movieList)
      Future.successful(Done)
    }

  }

}
