package learn.akka.stream.integration.part2

import scala.util.Random

/**
  * Created by gabbi on 25.06.17.
  */
case class WindTurbineData(id: String) {
  val random = Random

  def getNext: String = {
    val timeStamp = System.currentTimeMillis() / 1000
    val power: String = f"${random.nextDouble() * 10}%.2f"
    val rotorSpeed: String = f"${random.nextDouble() * 10}%.2f"
    val windSpeed: String = f"${random.nextDouble() * 100}%.2f"

    s"""
       |{
       |  "id" : $id,
       |  "timestamp" : $timeStamp,
       |  "measurements":{
       |    "power" : $power,
       |    "rotorSpeed" : $rotorSpeed,
       |    "windSpeed" : $windSpeed
       |  }
       |}
     """.stripMargin
  }
}
