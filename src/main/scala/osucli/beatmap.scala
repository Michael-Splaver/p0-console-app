package osucli

import org.bson.types.ObjectId

case class beatmap(_id: ObjectId, title: String, artist: String, difficulty: Double, maxComboAvailable: Int)

object beatmap {
  def apply(title: String, artist: String, difficulty: Double, maxComboAvailable: Int): beatmap = {
    new beatmap(new ObjectId, title, artist, difficulty, maxComboAvailable)
  }
}
