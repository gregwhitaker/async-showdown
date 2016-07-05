/*
 * Copyright 2016 Greg Whitaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.gregwhitaker.asyncshowdown

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.core.scenario.Simulation

/**
  * Started with a command like:
  * $ cd $GATLING_HOME/bin
  * $ ./gatling.sh -s "asyncshowdown.AsyncShowdownSimulation"
  */
class AsyncShowdownSimulation extends Simulation {

  val rampUpTimeSecs = 60
  val testTimeSecs   = 120
  val noOfUsers      = 3000

  val baseURL      = "http://localhost:8080"
  val baseName     = "asyncshowdown"
  val requestName  = baseName + "-request"
  val scenarioName = baseName + "-scenario"
  val URI          = "/hello"
  //val URI          = "/helloblocking"

  val httpProtocol = http.baseURL(baseURL)

  val http_headers = Map(
    "Accept-Encoding" -> "gzip,deflate",
    "Content-Type" -> "text/json;charset=UTF-8",
    "Keep-Alive" -> "115")

  val scn = scenario(scenarioName)
    .during(testTimeSecs) {
      exec(
        http(requestName)
          .get(URI)
          .headers(http_headers)
          .check(status.is(200))
      ).pause(1 second)
    }

  setUp(scn.inject(atOnceUsers(noOfUsers)))
    .throttle(
      reachRps(3000) in (10 seconds),
      holdFor(2 minutes)
    ).protocols(httpProtocol)
}