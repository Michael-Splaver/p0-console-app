package osucli.dao

import org.mongodb.scala.{MongoCollection, Observable}
import org.mongodb.scala.model.Filters._
import osucli.models.user
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}


class userDAO (collection: MongoCollection[user]){

  //helper function
  def getResults[T](obs: Observable[T]): Seq[T] = {
    Await.result(obs.toFuture(), Duration(10, SECONDS))
  }

  def createUser (user: user): Unit = {
    getResults(collection.insertOne(user))
  }

  def deleteUser (username: String) : Unit = {
    getResults(collection.findOneAndDelete(equal("userName", username)))
  }

  def getUser (username: String) : Option[user] = {
    //printResults(collection.find(equal("userName", username)))
    getResults(collection.find(equal("userName", username))).headOption
  }

  def getUsers () : Seq[user] = {
    getResults(collection.find())
  }
}
