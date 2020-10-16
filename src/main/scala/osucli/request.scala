package osucli

import net.liftweb
import net.liftweb.json.{DefaultFormats, JValue, parse}
import net.liftweb.json.JsonAST.JValue
import scalaj.http.{Http, HttpOptions}

import scala.io.Source
import scala.util.{Try, Using}



object request {
  private implicit val formats : DefaultFormats.type = DefaultFormats
  private val configSource : Try[String] = Using(Source.fromFile("config.json")) { _.mkString}
  private val config : JValue = parse(configSource.get)
  private val api_key : String = (config\"api-key").extractOpt[String].get

  def getUserFromAPI(username: String): JValue = {
    val result = Http(s"https://osu.ppy.sh/api/get_user?k=$api_key&u=$username").postForm
      .header("Content-Type", "application/x-www-form-urlencoded")
      .option(HttpOptions.readTimeout(10000)).asString

    liftweb.json.parse(result.body)
  }

  def getUserBestFromAPI(username: String): JValue = {
    val result = Http(s"https://osu.ppy.sh/api/get_user_best?k=$api_key&u=$username").postForm
      .header("Content-Type", "application/x-www-form-urlencoded")
      .option(HttpOptions.readTimeout(10000)).asString

    liftweb.json.parse(result.body)
  }

  def getBeatmapFromAPI(beatmapid: Int): JValue = {
    val result = Http(s"https://osu.ppy.sh/api/get_beatmaps?k=$api_key&b=$beatmapid").postForm
      .header("Content-Type", "application/x-www-form-urlencoded")
      .option(HttpOptions.readTimeout(10000)).asString

    liftweb.json.parse(result.body)
  }
}
