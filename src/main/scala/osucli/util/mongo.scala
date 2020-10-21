package osucli.util

import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import osucli.dao.{beatmapDAO, userDAO}
import osucli.models.{beatmap, play, user}
import scala.reflect.ClassTag

class mongo () {
  private val codecRegistry = fromRegistries(fromProviders(classOf[user], classOf[play], classOf[beatmap]), MongoClient.DEFAULT_CODEC_REGISTRY)
  private val client: MongoClient = MongoClient()
  private val db: MongoDatabase = client.getDatabase("osucli_db").withCodecRegistry(codecRegistry)

  def getMongoCollection[T:ClassTag](collectionName: String): MongoCollection[T] = {
    db.getCollection(collectionName)
  }

  def addUser(user: user) : Unit = {
    val dao = new userDAO(getMongoCollection("user"))
    dao.createUser(user)
  }

  def deleteUser(username: String): Unit = {
    val topten = getUserByUsername(username).get.topTenPlays.toList
    topten.foreach(x => deleteBeatmap(x.beatmap_id.toString))

    val dao = new userDAO(getMongoCollection("user"))
    dao.deleteUser(username)
  }

  def getAllUsers() : Seq[user] = {
    val dao = new userDAO(getMongoCollection("user"))
    dao.getUsers()
  }

  def getUserByUsername(username: String) : Option[user] = {
    val dao = new userDAO(getMongoCollection("user"))
    dao.getUser(username)
  }

  def getBeatmap(beatmap_id: String) : Option[beatmap] = {
    val dao = new beatmapDAO(getMongoCollection("beatmap"))
    dao.getBeatmap(beatmap_id)
  }

  def deleteBeatmap(beatmap_id: String) : Unit = {
    val dao = new beatmapDAO(getMongoCollection("beatmap"))
    dao.deleteBeatmap(beatmap_id)
  }

  def addBeatmap(beatmap: beatmap) : Unit = {
    val dao = new beatmapDAO(getMongoCollection("beatmap"))
    dao.createBeatmap(beatmap)
  }
}


