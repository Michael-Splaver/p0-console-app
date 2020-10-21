package osucli.dao

import org.mongodb.scala.{MongoCollection, Observable}
import org.mongodb.scala.model.Filters._
import osucli.models.user

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}


class userDAO (collection: MongoCollection[user]){

  //helper functions for access and printing, to get us started + skip the Observable data type
  def getResults[T](obs: Observable[T]): Seq[T] = {
    Await.result(obs.toFuture(), Duration(10, SECONDS))
  }

  def printResults[T](obs: Observable[T]): Unit = {
    getResults(obs).foreach(println(_))
  }

  def createUser (user: user): Unit = {
    Await.result(collection.insertOne(user).toFuture(), Duration(10,SECONDS))
  }
  //def deleteUser () : user = {}
  //def updateUser () : user = {}
  def getUser (username: String) : Option[user] = {
    //printResults(collection.find(equal("userName", username)))
    Await.result(collection.find(equal("userName", username)).toFuture(), Duration(10,SECONDS)).headOption
  }
  def getUsers () : Seq[user] = {
    Await.result(collection.find().toFuture(), Duration(10,SECONDS))
  }
}
