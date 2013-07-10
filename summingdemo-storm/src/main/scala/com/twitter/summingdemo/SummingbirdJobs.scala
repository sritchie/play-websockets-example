/*
 Copyright 2013 Twitter, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.twitter.summingdemo

import com.twitter.summingbird._
import com.twitter.summingbird.batch.Batcher
import twitter4j.{ Status, TwitterStreamFactory }
import twitter4j.conf.ConfigurationBuilder

object SummingbirdJobs {
  /**
    * These two items are required to run Summingbird in
    * batch/realtime mode, across the boundary between storm and
    * scalding jobs.
    */
  implicit val timeOf: TimeExtractor[Status] =
    TimeExtractor(_.getCreatedAt.getTime)

  def tokenize(text: String) : TraversableOnce[String] =
    text.toLowerCase
      .replaceAll("[^a-zA-Z0-9\\s]", "")
      .split("\\s+")

  def wordCount[P <: Platform[P]](
    source: Producer[P, Status],
    store: P#Store[String, Long]) =
    source
      .flatMap { tweet: Status => tokenize(tweet.getText).map(_ -> 1L) }
      .sumByKey(store)

  def letterCount[P <: Platform[P]](
    source: Producer[P, Status],
    store: P#Store[String, Long]) =
    source.flatMap { tweet: Status =>
      tokenize(tweet.getText).flatMap(_.toSeq.map(_.toString -> 1L))
    }.sumByKey(store)

  def tweetCount[P <: Platform[P]](
    source: Producer[P, _],
    store: P#Store[String, Long]) =
    source
      .map(_ => Storage.globalKey -> 1L)
      .sumByKey(store)
}
