package osucli

import scala.io.StdIn
import scala.util.matching.Regex
import org.backuity.ansi.AnsiFormatter.FormattedHelper

import scala.util.{Try, Using}
import scala.io.Source
import net.liftweb.json._

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

object cli {
  private var continueMenuLoop = true
  private val commandArgPattern : Regex = "(\\w+)\\s*(.*)".r   //cmd, (args0,args1,args2,...)

  def welcomeMessage() : Unit = {
    println(ansi"%bold{%magenta{✧･ﾟ: *✧･ﾟ:* %underline{Welcome to osu!cli} *:･ﾟ✧*:･ﾟ✧}}")
    println(ansi"%yellow{type help for a list of commands.}")
  }

  val commandsList = Map(
    "exit" -> "exit the osu!cli application",
    "help" -> "get the list of commands or specific command information"
  )

  def start(): Unit = {
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
            case "create" => createNewUser()
            case notFound => println(ansi"%red{%bold{$notFound} is not a recognized command.}")
          }
        case "" => //empty line -> do nothing
        case notRecognized
        => println(ansi"%red{%bold{$notRecognized} is not a recognized command format, use [command] <args>}")
      }
    }
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
  implicit val formats = DefaultFormats
  def createNewUser() : Unit = {

    val userJson = request.getUserFromAPI("mikacutie")
    val userBestJson = request.getUserBestFromAPI("mikacutie")

    val topTenPlaysList = (userBestJson).extract[List[JObject]]

    var topTenPlays : Set[play] = Set.empty
    for (x <- topTenPlaysList.indices){
      //request.getBeatmapFromAPI((topTenPlaysList(x)\"beatmap_id").extract[String].toInt)
      topTenPlays += play(
        (topTenPlaysList(x)\"maxcombo").extract[String].toInt,
        (topTenPlaysList(x)\"rank").extract[String],
        (topTenPlaysList(x)\"pp").extract[String].toDouble
      )
    }

    val userName = (userJson\"username").extract[String]
    val playCount = (userJson\"playcount").extract[String].toInt
    val rank = (userJson\"pp_rank").extract[String].toInt
    val performancePoints = (userJson\"pp_raw").extract[String].toDouble
    val accuracy = (userJson\"accuracy").extract[String].toDouble
    val secondsPlayed = (userJson\"total_seconds_played").extract[String].toInt

    val newUser : user = user(
      userName, playCount, rank, performancePoints, accuracy, secondsPlayed, topTenPlays
    )
    mongo.createUser(newUser)
  }
}