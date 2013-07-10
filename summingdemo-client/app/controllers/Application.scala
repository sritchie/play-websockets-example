package controllers

import play.api._
import play.api.mvc._
import models.Example
import play.api.libs.json.{ JsArray, Json, JsValue, JsObject, JsString }
import models.Statistics
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Promise
import scala.concurrent.duration.DurationInt
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {
  /**
    * The main action. Loads up a list of examples.
    */
  def index = Action {
    Ok(views.html.dashboard("Summingbird Tutorials", Example.examples))
  }

  def example(id: String, keyString: String) = Action { implicit request =>
    Example.examples.find(_._1 == id) match {
      case Some((id, _)) => Ok(views.html.example(id, keyString))
      case None => NoContent
    }
  }

  def initialize(id: String, keyString: String) = {
    val keys = keyString.split(",").toSet
    WebSocket.async[JsValue] { request =>
      Example.examples.find(_._1 == id) match {
        case Some((_, example)) => Statistics.attach(example, keys)
        case None => {
          val enumerator =
            Enumerator.generateM[JsValue](Promise.timeout(None, 1.second))
              .andThen(Enumerator.eof)
          Promise.pure(Iteratee.ignore[JsValue] -> enumerator)
        }
      }
    }
  }
}
