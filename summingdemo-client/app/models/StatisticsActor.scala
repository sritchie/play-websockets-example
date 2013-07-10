package models

import scala.util.Random
import com.twitter.summingdemo.Storage
import com.twitter.summingbird.store.ClientStore
import com.twitter.storehaus.FutureOps
import com.twitter.storehaus.ReadableStore

import akka.actor.Actor
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{ JsNumber, JsObject, JsString, JsValue }

import Storage.batcher, ReadableStore.enrich

case class StringLongActor(example: Example, keys: Set[String]) extends Actor {
  val (enumerator, channel) = Concurrent.broadcast[JsValue]

  lazy val store: ReadableStore[String, Long] =
    ClientStore(Storage.stringLongStore, 3)

  def receive = {
    case Connect(host) => sender ! Connected(enumerator)
    case Refresh => {
      val timestamp = System.currentTimeMillis
      FutureOps.mapCollect(store.multiGet(keys))
        .foreach { m: Map[String, Option[Long]] =>
        channel.push {
          JsObject(
            Seq(
              "timestamp" -> JsNumber(timestamp),
              "pairs" -> example.transform(m))
          )
        }
      }
    }
  }
}
