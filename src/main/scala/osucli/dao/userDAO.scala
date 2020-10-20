package osucli.dao

import org.mongodb.scala.MongoCollection
import osucli.models.user

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}


class userDAO (collection: MongoCollection[user]){
  def createUser (user: user): Unit = {
    Await.result(collection.insertOne(user).toFuture(), Duration(10,SECONDS))
  }
  //def deleteUser () : user = {}
  //def updateUser () : user = {}
  /*def getUser (username: String) : user = {
    getResult(collection.find(equal("username", username)))(0)
  }*/
}
