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

  val commandsList = Map(
    "exit" -> "exit the osu!cli application",
    "help" -> "get the list of commands or specific command information",
    "add" -> "add a user to the application"
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
            case "add" => addUser(args)
            case "bool" => println(getPromptedBooleanInput("Would you like to play a game..?"))
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

    println(s"$prompt Please enter (Y/N)")
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
        case _ => println("Invalid input, please enter Y for yes or N for no.")
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
        ansi"%cyan{The following are all available commands}"
        commandsList.foreach(element => {
          println(ansi"%yellow{%bold{${element._1}}: ${element._2}}")
        })
      case 1 =>
        val info = commandsList.get(args(0))
        if (info.isDefined){
          println(ansi"%yellow{%bold{${args(0)}}: ${info.get}}")
        }
        else {
          println(ansi"%red{That's not a recognized command!}")
        }
      case _ => println(ansi"%red{Too many arguments! Use} %yellow{help} %red{or} %yellow{help [commandName]}")
    }
  }

  //this should be broken up and handled somewhere else

  def addUser(args: Array[String]) : Unit = {

    if (args.length != 1) {
      println(ansi"%red{Incorrect Number of arguments! Use} %yellow{add [username]}")
      return
    }

    val username = args(0)
    val userJson = parseJson(request.getUserFromAPI(username))
    val userBestJson = parseJson(request.getUserBestFromAPI(username))

    if(checkIfEmpty(userJson)){
      println(ansi"%red{Not a valid username! Use} %yellow{add [username]}")
      return
    }

    val userBestPlaysWithBeatmap = buildBestPlaysSetWithBeatmap(userBestJson)

    userBestPlaysWithBeatmap.foreach(x => mongo.addUpdateBeatmap(x.beatmap))

    val newUser = buildUser(userJson,buildPlaysWithPlaysBeatmap(userBestPlaysWithBeatmap))

    mongo.addUpdateUser(newUser)
  }
}