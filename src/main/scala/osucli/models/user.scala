package osucli.models

import org.bson.types.ObjectId

case class user(
  _id: ObjectId,
  userName: String,
  playCount: Int,
  rank: Int,
  performancePoints: Double,
  accuracy: Double,
  secondsPlayed: Int,
  topTenPlays: Set[play]
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

case class play(_id: ObjectId, beatmap_id: ObjectId, maxComboReached: Int, rank: String, performancePoints: Double)

object play {
  def apply(beatmap_id: ObjectId,
            maxComboReached: Int,
            rank: String,
            performancePoints: Double
           ): play = {
    new play(new ObjectId(), beatmap_id, maxComboReached, rank, performancePoints)
  }

  def apply(playBeatmap: playBeatmap): play = {
    new play(new ObjectId(), playBeatmap.beatmap._id, playBeatmap.maxComboReached, playBeatmap.rank, playBeatmap.performancePoints)
  }
}
//////////////
case class playBeatmap(beatmap: beatmap, maxComboReached: Int, rank: String, performancePoints: Double)

object playBeatmap{
  def apply(beatmap: beatmap, maxComboReached: Int, rank: String, performancePoints: Double): playBeatmap = {
    new playBeatmap(beatmap, maxComboReached, rank, performancePoints)
  }
}
