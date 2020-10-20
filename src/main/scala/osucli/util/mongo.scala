package osucli.util

import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import osucli.dao.{beatmapDAO, userDAO}
import osucli.models.{beatmap, play, user}

import scala.reflect.ClassTag

class mongo () {
  private val codecRegistry = fromRegistries(fromProviders(classOf[user]), fromProviders(classOf[play]), fromProviders(classOf[beatmap]), MongoClient.DEFAULT_CODEC_REGISTRY)
  private val client: MongoClient = MongoClient()
  private val db: MongoDatabase = client.getDatabase("osucli_db").withCodecRegistry(codecRegistry)

  def getMongoCollection[T:ClassTag](collectionName: String): MongoCollection[T] = {
    db.getCollection(collectionName)
  }

  /*def getResult[T](obs: Observable[T]): Seq[T] = {
    Await.result(obs.toFuture(), Duration(10,SECONDS))
  }*/

  /**Adds the user to mongoDB or updates user*/
  def addUpdateUser(user: user) : Unit = {
    //check if user exists
    //userDAO
    val dao = new userDAO(getMongoCollection("user"))
    dao.createUser(user)
  }

  def addUpdateBeatmap(beatmap: beatmap) : Unit = {
    val dao = new beatmapDAO(getMongoCollection("beatmap"))
    dao.createBeatmap(beatmap)
  }
}


