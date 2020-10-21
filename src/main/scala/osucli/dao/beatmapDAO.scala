package osucli.dao

import org.bson.types.ObjectId
import org.mongodb.scala.{MongoCollection, Observable}
import org.mongodb.scala.model.Filters._
import osucli.models.beatmap
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}


class beatmapDAO (collection: MongoCollection[beatmap]){

  //helper function
  def getResults[T](obs: Observable[T]): Seq[T] = {
    Await.result(obs.toFuture(), Duration(10, SECONDS))
  }

  def createBeatmap (beatmap: beatmap): Unit = {
    getResults(collection.insertOne(beatmap))
  }

  def deleteBeatmap (beatmap_id: String): Unit = {
    getResults(collection.findOneAndDelete(equal("_id", new ObjectId(beatmap_id))))
  }

  def getBeatmap (beatmap_id: String) : Option[beatmap] = {
    getResults(collection.find(equal("_id", new ObjectId(beatmap_id)))).headOption
  }

  def getBeatmap (beatmap: beatmap) : Option[beatmap] = {
    getResults(collection.find(and(equal("title", beatmap.title),equal("difficulty", beatmap.difficulty)))).headOption
  }
}
