package makelainen.mobro

import akka.actor.ActorSystem

/**
 * This enables dependency injection via self type annotation.
 */
trait ActorSystemComponent {

  implicit val actorSystem: ActorSystem
}