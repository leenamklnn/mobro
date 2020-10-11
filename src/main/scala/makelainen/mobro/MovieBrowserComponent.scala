package makelainen.mobro

import akka.actor.ActorSystem

/**
 * Cake pattern component for Movie Browser
 */
trait MovieBrowserComponent extends HttpServerComponent {

  val movieBrowser: MovieBrowser
  override implicit val actorSystem: ActorSystem = ActorSystem("MovieBrowser")

  override val httpServer: HttpServer = new HttpServer()

  /**
   * The Movie Browser instance class that starts.
   *
   * Maybe a bit of overkill in this version of the app but could include other things included in
   * application startup and shutdown.
   */
  class MovieBrowser {

    private[mobro] def startup(): Unit = {
      httpServer.start()
    }

    private[mobro] def shutdown(): Unit = {
      httpServer.stop()
    }

  }

}
