package makelainen.mobro

import akka.actor.Terminated
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
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


    //TODO: Logging instead of println

    implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
    val decider: Supervision.Decider = { t: Throwable =>
      Supervision.Resume
    }

    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider))

    var binding: Option[Future[Http.ServerBinding]] = None

    //TODO: See if all of these are needed and make some restrictions
    val headers = List(
      RawHeader("Access-Control-Allow-Headers", "*"),
      RawHeader("Access-Control-Allow-Origin", "*"),
      RawHeader("Access-Control-Allow-Method", "*"),
      RawHeader("Access-Control-Request-Method", "*"),
      RawHeader("Access-Control-Allow-Credentials", "true"),
      RawHeader("Access-Control-Max-Age", "86400"))

    /**
     * Route for fetching movies from the movies data source
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
                entity = jsonEntity(s"""{"message":"Fetching movies failed."}""")))
          }
          complete(HttpResponse(
            StatusCodes.OK,
            entity = jsonEntity(objectMapper.writeValueAsString(movies))))
        } ~
          pathPrefix(Segment) {
            name =>
              println("Get movie with name " + name)
              val movie = try {
                fileHandler.getMovieFromFile(name)
              } catch {
                case t: Throwable =>
                  complete(HttpResponse(
                    StatusCodes.InternalServerError,
                    entity = jsonEntity(s"""{"message":"Getting movie failed."}""")))
              }
              complete(HttpResponse(
                StatusCodes.OK,
                entity = jsonEntity(objectMapper.writeValueAsString(movie))))
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
                  complete(HttpResponse(
                    StatusCodes.InternalServerError,
                    entity = jsonEntity(s"""{"message":"Adding movie failed, see logs for more info."}""")))
              }
            }
          }
        } ~
        options {
          complete(HttpResponse(
            StatusCodes.OK,
            entity = jsonEntity(
              objectMapper.writeValueAsString(s"""{"message":"You said options, I say 200 OK."}"""))))
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
        } ~
          options {
            complete(HttpResponse(
              StatusCodes.OK,
              entity = jsonEntity(
                objectMapper.writeValueAsString(s"""{"message":"You said options, I say 200 OK."}"""))))
          }
      }

    val routes: Route = pathPrefix("mobro") {
      respondWithDefaultHeaders(headers) {
        routeFetchAll ~
          routeAdd ~
          routeRemove
      }

    }

    def jsonEntity(json: String): HttpEntity.Strict = HttpEntity(ContentTypes.`application/json`, json)

    /**
     * Start the HttpServer
     */
    def start(): Option[Future[Http.ServerBinding]] = {
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
      println(s"Stopping the server.")
      binding match {
        case Some(b) => b.flatMap(_.unbind)
        case None    => Future.successful[Unit](())
      }
      actorSystem.terminate()
    }

  }

}

