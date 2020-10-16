package osucli

import org.mongodb.scala.{Completed, Document, MongoClient, MongoCollection, Observable, ObservableImplicits, Observer, Subscription}
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.{MongoClient, MongoCollection}
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

object mongo {

  def getResult[T](obs: Observable[T]): Seq[T] ={
    Await.result(obs.toFuture(), Duration(10,SECONDS))
  }

  //break out to separate userAccess object later
  def createUser (user: user): Unit = {
    val codecRegistry = fromRegistries(fromProviders(classOf[user]), fromProviders(classOf[play]), MongoClient.DEFAULT_CODEC_REGISTRY)
    val client = MongoClient()
    val db = client.getDatabase("osucli_db").withCodecRegistry(codecRegistry)

    val collection : MongoCollection[user] = db.getCollection("user")

    getResult(collection.insertOne(user))

    client.close()
  }
  //def deleteUser () : user = {}
  //def updateUser () : user = {}
  //def getUser () : user = {}
}
