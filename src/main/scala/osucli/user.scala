package osucli

import org.bson.types.ObjectId

case class osuMap(title: String, artist: String, difficulty: Double, maxComboAvailable: Double)

object osuMap {
  def apply(title: String, artist: String, difficulty: Double, maxComboAvailable: Double): osuMap = {
    new osuMap(title, artist, difficulty, maxComboAvailable)
  }
}

/*case class play(mapPlayed: osuMap, maxComboReached: Int, rank: Char, performancePoints: Double)

object play{
  def apply(mapPlayed: osuMap, maxComboReached: Int, rank: Char, performancePoints: Double): play = {
    new play(mapPlayed, maxComboReached, rank, performancePoints)
  }
}*/

case class play(maxComboReached: Int, rank: String, performancePoints: Double)

object play{
  def apply(maxComboReached: Int, rank: String, performancePoints: Double): play = {
    new play(maxComboReached, rank, performancePoints)
  }
}

case class user(
  _id: ObjectId,
  userName: String,
  playCount: Int,
  rank: Int,
  performancePoints: Double,
  accuracy: Double,
  secondsPlayed: Int,
  topTenPlays: Set[play]{
  }
)

object user{
  def apply(userName: String,
            playCount: Int,
            rank: Int,
            performancePoints: Double,
            accuracy: Double,
            secondsPlayed: Int,
            topTenPlays: Set[play]
           ): user = {
    new user(new ObjectId(), userName, playCount, rank, performancePoints, accuracy, secondsPlayed, topTenPlays)
  }
}