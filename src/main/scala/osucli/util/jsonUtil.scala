package osucli.util

import net.liftweb
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.{DefaultFormats, JObject}
import osucli.{models, _}
import osucli.models.{beatmap, play, playBeatmap, user}

object jsonUtil {

  implicit val formats = DefaultFormats

  def buildUser(json: JValue, topTenPlays: Set[play]): user = {
    val userName = (json \ "username").extract[String]
    val playCount = (json \ "playcount").extract[String].toInt
    val rank = (json \ "pp_rank").extract[String].toInt
    val performancePoints = (json \ "pp_raw").extract[String].toDouble
    val accuracy = (json \ "accuracy").extract[String].toDouble
    val secondsPlayed = (json \ "total_seconds_played").extract[String].toInt

    val newUser: user = models.user(
      userName, playCount, rank, performancePoints, accuracy, secondsPlayed, topTenPlays
    )

    return newUser
  }

  def buildBestPlaysSetWithBeatmap(topTenPlaysJson: JValue): Set[playBeatmap] = {
    val topTenPlaysList = topTenPlaysJson.extract[List[JObject]]
    var topTenPlays: Set[playBeatmap] = Set.empty
    for (x <- topTenPlaysList.indices) {
      val newBeatmapJson = parseJson(request.getBeatmapFromAPI((topTenPlaysList(x) \ "beatmap_id").extract[String].toInt))

      val newBeatmap: beatmap = beatmap(
        (newBeatmapJson \ "title").extract[String],
        (newBeatmapJson \ "artist").extract[String],
        (newBeatmapJson \ "difficultyrating").extract[String].toDouble,
        (newBeatmapJson \ "max_combo").extract[String].toInt,
      )

      topTenPlays += models.playBeatmap(
        newBeatmap,
        (topTenPlaysList(x) \ "maxcombo").extract[String].toInt,
        (topTenPlaysList(x) \ "rank").extract[String],
        (topTenPlaysList(x) \ "pp").extract[String].toDouble
      )
    }

    return topTenPlays
  }

  def buildPlaysWithPlaysBeatmap(playsWithBM: Set[playBeatmap]): Set[play] = {
    var plays: Set[play] = Set.empty
    //playsWithBM.toList.foreach(play(_))
    playsWithBM.foreach(plays += models.play(_))
    return plays
  }


  def checkIfEmpty(json: JValue): Boolean = {
    return json.extract[List[JObject]].isEmpty
  }

  def parseJson(json: String): JValue = {
    liftweb.json.parse(json)
  }
}
