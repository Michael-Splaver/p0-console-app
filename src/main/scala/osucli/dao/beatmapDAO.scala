package osucli.dao

import org.mongodb.scala.MongoCollection
import osucli.models.beatmap

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}


class beatmapDAO (collection: MongoCollection[beatmap]){

  def createBeatmap (beatmap: beatmap): Unit = {
    Await.result(collection.insertOne(beatmap).toFuture(), Duration(10,SECONDS))
  }
}
