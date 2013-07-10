package models

import play.api.libs.json.{ JsNumber, JsObject }

object Example {
  def ratios[K](m: Map[K, Option[Long]]): Map[K, Float] = {
    val newM: Map[K, Float] = m.map { case (k, v) =>
      k -> v.map(_.toFloat).getOrElse(0.0f)
    }
    val sum = newM.values.foldLeft(0.0)(_ + _)
    if (sum == 0.0)
      newM
    else
      newM.mapValues(_.toDouble / sum * 100 toFloat)
  }

  lazy val examples: List[(String, Example)] =
    List(
      "counter" -> Counter("Counter"),
      "ratio" -> Ratio("Ratio")
    )
}

sealed trait Example {
  def description: String
  def transform(m: Map[String, Option[Long]]): JsObject
}

case class Counter(description: String) extends Example {
  def transform(m: Map[String, Option[Long]]) =
    JsObject {
      m.mapValues(l => JsNumber(l.getOrElse(0L): Long)).toSeq
    }
}
case class Ratio(description: String) extends Example {
  def transform(m: Map[String, Option[Long]]) =
    JsObject {
      Example.ratios(m).mapValues(JsNumber(_)).toSeq
    }
}
