package makelainen.mobro

import akka.actor.Terminated
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.{complete, get, path, pathPrefix, post, _}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

/**
 * Cake pattern component for the HttpServer
 */
trait HttpServerComponent extends ActorSystemComponent
  with ObjectMapper
  with FileHandlerComponent
  with MoviesJsonSupport {

  val httpServer: HttpServer
  val fileHandler: FileHandler = new FileHandler
  val settings: ApplicationSettings = new ApplicationSettings

  /**
   * HttpServer that provides the REST API
   */
  class HttpServer() {

    implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
    val decider: Supervision.Decider = { t: Throwable =>
      Supervision.Resume
    }

    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider))

    var binding: Option[Future[Http.ServerBinding]] = None

    /**
     * Route for fetching all movies in the movies data source
     */
    val routeFetchAll: Route =
      path("movies") {
        get {

          val movies = try {
            println("Fetch movies route")
            fileHandler.readMoviesFromFile()
          } catch {
            case t: Throwable =>
              complete(HttpResponse(
                StatusCodes.InternalServerError,
                entity = jsonEntity(s"""{"message":"Fetching movie failed."}""")))
          }
          complete(HttpResponse(
            StatusCodes.OK,
            entity = jsonEntity(objectMapper.writeValueAsString(movies))))
        }
      }

    /**
     * Route for adding a movie to the movies data source
     */
    val routeAdd: Route =
      path("add") {
        post {
          println("Add movie route")
          entity(as[Movie]) {
            movie => {
              //TODO: Validate fields
              println("movie: " + movie)
              val future = fileHandler.appendMovieToFile(movie)
              onComplete(future) {
                case Success(_) =>
                  complete(HttpResponse(
                    StatusCodes.OK,
                    entity = jsonEntity(
                      objectMapper.writeValueAsString(s"""{"message":"Movie successfully added."}"""))))
                case Failure(ex) =>
                  println(s"Adding movie failed: $ex")
                  //TODO: Logging
                  complete(HttpResponse(
                    StatusCodes.InternalServerError,
                    entity = jsonEntity(s"""{"message":"Adding movie failed, see logs for more info."}""")))
              }
            }
          }
        }
      }

    /**
     * Route for removing a movie from the movies data source
     */
    val routeRemove: Route =
      path("remove") {
        post {
          println("Remove movies route")
          entity(as[RemoveMovieRequest]) {
            movieName => {
              val future = fileHandler.removeMovieFromFile(movieName.name)
              onComplete(future) {
                case Success(_) =>
                  complete(HttpResponse(
                    StatusCodes.OK,
                    entity = jsonEntity(
                      objectMapper.writeValueAsString(s"""{"message":"Movie successfully removed."}"""))))
                case Failure(ex) =>
                  println(s"Removing movie failed: $ex")
                  //TODO: Logging
                  complete(HttpResponse(
                    StatusCodes.InternalServerError,
                    entity = jsonEntity(s"""{"message":"Removing movie failed, see logs for more info."}""")))
              }
            }
          }
        }
      }

    val routes: Route = pathPrefix("mobro") {
      routeFetchAll ~
      routeAdd ~
      routeRemove
    }

    def jsonEntity(json: String): HttpEntity.Strict = HttpEntity(ContentTypes.`application/json`, json)

    /**
     * Start the HttpServer
     */
    def start(): Option[Future[Http.ServerBinding]] = {
      //TODO: logging
      println(s"Starting server at ${
        settings.bindAddress
      }:${
        settings.bindPort
      }. Welcome.")
      binding = Some(Http().bindAndHandle(routes, settings.bindAddress, settings.bindPort))
      binding
    }

    /**
     * Stop the HttpServer
     */
    def stop(): Future[Terminated] = {
      //TODO: logging
      println(s"Stopping the server.")
      binding match {
        case Some(b) => b.flatMap(_.unbind)
        case None    => Future.successful[Unit](())
      }
      actorSystem.terminate()
    }

  }

}

