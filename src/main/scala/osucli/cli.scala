package osucli

import java.util.logging.{Level, Logger}

import scala.io.StdIn
import scala.util.matching.Regex
import org.backuity.ansi.AnsiFormatter.FormattedHelper

import scala.util.{Try, Using}
import scala.io.Source
import net.liftweb.json._

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}
import osucli.util.jsonUtil._
import osucli.util.{mongo, request}

object cli {
  private var continueMenuLoop : Boolean = true
  private val commandArgPattern : Regex = "(\\w+)\\s*(.*)".r   //cmd, (args0,args1,args2,...)
  private var mongo: mongo = null

  def init() : Unit = {
    setLoggingLevel(Level.WARNING)
    mongo = new mongo
  }

  def welcomeMessage() : Unit = {
    println(ansi"%bold{%magenta{✧･ﾟ: *✧･ﾟ:* %underline{Welcome to osu!cli} *:･ﾟ✧*:･ﾟ✧}}")
    println(ansi"%yellow{type help for a list of commands.}")
  }

  def setLoggingLevel(level: Level): Unit = {
    val logger = Logger.getLogger("")
    logger.setLevel(level)
  }

  def buildTimeString(seconds: Int): String = {
    var hours = seconds / 60
    val minutes = hours % 60
    hours /= 60
    s"$hours hours and $minutes minutes "
  }

  val commandsList = Map(
    "exit" -> "exit the osu!cli application",
    "help" -> "get the list of commands or specific command information",
    "add" -> "add an osu user to the application",
    "update" -> "update an osu user to the application",
    "list" -> "list the users in the application",
    "leaderboard" -> "display the top 10 rated users in the application",
    "info" -> "display the information of a user"

  )

  def start(): Unit = {
    init()
    welcomeMessage()
    while(continueMenuLoop){
      StdIn.readLine() match {
        case commandArgPattern(cmd, arg) =>
          val command = cmd.toLowerCase
          var args = Array[String]()
          if (!arg.isEmpty) {args = arg.split(" ")}
          command match {
            case "help" => help(args)
            case "exit" => exit()
            case "add" => add(args)
            case "update" => add(args)
            case "info" => info(args)
            case "list" => list()
            case "leaderboard" => leaderboard()
            case "bool" => println(getPromptedBooleanInput("would you like to play a game..?"))
            case notFound => println(ansi"%red{%bold{$notFound} is not a recognized command.}")
          }
        case "" => //empty line -> do nothing
        case notRecognized
        => println(ansi"%red{%bold{$notRecognized} is not a recognized command format, use [command] <args>}")
      }
    }
  }

  def getPromptedBooleanInput(prompt: String) : Boolean = {
    var bool = false
    var continueInputLoop = true

    println(s"$prompt please enter (Y/N)")
    while (continueInputLoop) {
      StdIn.readLine().toLowerCase match {
        case "y" => {
          bool = true
          continueInputLoop = false
        }
        case "n" => {
          bool = false
          continueInputLoop = false
        }
        case _ => println("invalid input, please enter Y for yes or N for no.")
      }
    }
    return bool
  }

  def exit() : Unit = {
    println(ansi"%magenta{Goodbye!}")
    continueMenuLoop = false
  }

  def help(args: Array[String]) : Unit = {
    args.length match {
      case 0 =>
        ansi"%cyan{the following are all available commands}"
        commandsList.foreach(element => {
          println(ansi"%yellow{%bold{${element._1}}: ${element._2}}")
        })
      case 1 =>
        val info = commandsList.get(args(0))
        if (info.isDefined){
          println(ansi"%yellow{%bold{${args(0)}}: ${info.get}}")
        }
        else {
          println(ansi"%red{that's not a recognized command!}")
        }
      case _ => println(ansi"%red{too many arguments! Use} %yellow{help} %red{or} %yellow{help [commandName]}")
    }
  }

  def leaderboard() : Unit = {
    
  }

  def info(args: Array[String]) : Unit = {
    if (args.length != 1) {
      println(ansi"%red{incorrect Number of arguments! Use} %yellow{info [username]}")
      return
    }
    val username = args(0)
    val userOptional = mongo.getUserByUsername(username)
    if (userOptional.isEmpty) {
      println(ansi"%red{couldn't find that user in the application!} %yellow{you may need to add the user}")
      return
    }
    val user = userOptional.get
    println(ansi"%yellow{%bold{${user.userName}}}")
    println(ansi"   %yellow{-global ranking: %bold{${user.rank}}}")
    println(ansi"   %yellow{-play count: %bold{${user.playCount}}}")
    println(ansi"   %yellow{-accuracy: %bold{${user.accuracy}}}")
    println(ansi"   %yellow{-performance points: %bold{${user.performancePoints}}}")
    println(ansi"   %yellow{-time played: %bold{${buildTimeString(user.secondsPlayed)}}}")
//    val id = args(0)
//    val beatmapOptional = mongo.getBeatmap(id)
//    println(beatmapOptional.get.title)
  }

  def list() : Unit = {
    println(ansi"%yellow{%bold{all available users:}}")
    val users = mongo.getAllUsers().toList
    for (x <- users){
      print(ansi"%yellow{${x.userName}}  ")
    }
    println("")
  }

  def add(args: Array[String]) : Unit = {
    //return if not 1 argument
    if (args.length != 1) {
      println(ansi"%red{incorrect Number of arguments! Use} %yellow{add [username]} %red{or} %yellow{update [username]}")
      return
    }
    val username = args(0)
    //POST request to osu API to get user information
    val userJson = parseJson(request.getUserFromAPI(username))
    //POST request to osu API to get user best plays information
    val userBestJson = parseJson(request.getUserBestFromAPI(username))
    //check if user exists, if it does return
    if(checkIfEmpty(userJson)){
      println(ansi"%red{not a valid username! Use} %yellow{add [username]}")
      return
    }
    //build the best plays objects with beatmaps
    val userBestPlaysWithBeatmap = buildBestPlaysSetWithBeatmap(userBestJson)
    //add or update all of the beatmaps from the best plays into the mongoDB
    userBestPlaysWithBeatmap.foreach(x => mongo.addUpdateBeatmap(x.beatmap))
    //build the user object with the user information and the best plays information embedded
    val newUser = buildUser(userJson,buildPlaysWithPlaysBeatmap(userBestPlaysWithBeatmap))
    //add or update user to mongoDB
    mongo.addUpdateUser(newUser)
    println(ansi"%yellow{user %bold{$username} successfully added!}")
  }
}