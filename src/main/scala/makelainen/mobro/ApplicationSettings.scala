package makelainen.mobro

import akka.actor.{ActorSystem, Extension}
import com.typesafe.config.{Config, ConfigFactory}

/**
 * Settings from application.conf
 */
class ApplicationSettings() extends Extension {

  val config: Config = ConfigFactory.load()

  implicit lazy val actorSystem: ActorSystem = ActorSystem("mobro", config)

  /** Webserver bind address. */
  val bindAddress: String = config.getString("webserver.bindAddress")

  /** Webserver bind port. */
  val bindPort: Int = config.getInt("webserver.bindPort")

}

