package makelainen.mobro

import scala.util.control.NonFatal

/**
 * Application bootstrap
 */
object Bootstrap extends App with MovieBrowserComponent {

  override val movieBrowser: MovieBrowser = new MovieBrowser

  try {
    movieBrowser.startup()
    sys.addShutdownHook {
      movieBrowser.shutdown()
    }
  } catch {
    case NonFatal(e) =>
      movieBrowser.shutdown()
  }
}

