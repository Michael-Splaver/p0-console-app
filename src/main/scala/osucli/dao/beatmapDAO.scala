package osucli.dao

import org.bson.types.ObjectId
import org.mongodb.scala.{MongoCollection, Observable}
import org.mongodb.scala.model.Filters._
import osucli.models.beatmap

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}


class beatmapDAO (collection: MongoCollection[beatmap]){

  //helper functions for access and printing, to get us started + skip the Observable data type
  def getResults[T](obs: Observable[T]): Seq[T] = {
    Await.result(obs.toFuture(), Duration(10, SECONDS))
  }

  def printResults[T](obs: Observable[T]): Unit = {
    getResults(obs).foreach(println(_))
  }

  def createBeatmap (beatmap: beatmap): Unit = {
    Await.result(collection.insertOne(beatmap).toFuture(), Duration(10,SECONDS))
  }

  def getBeatmap (beatmap_id: String) : Option[beatmap] = {
    getResults(collection.find(equal("_id", new ObjectId(beatmap_id)))).headOption
  }
}
