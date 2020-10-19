package osucli

import java.util.logging.{Logger,Level}

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

  def setLoggingLevel(level: Level): Unit = {
    val root = Logger.getLogger("")
    root.setLevel(level)
  }

  val commandsList = Map(
    "exit" -> "exit the osu!cli application",
    "help" -> "get the list of commands or specific command information"
  )

  def start(): Unit = {
    setLoggingLevel(Level.WARNING)
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
  def addUser(args: Array[String]) : Unit = {

    if (args.length != 1) {
      println(ansi"%red{Incorrect Number of arguments! Use} %yellow{add [username]}")
      return
    }

    val username = args(0)
    val userJson = request.getUserFromAPI(username)

    if(userJson.extract[List[JObject]].isEmpty){
      println(ansi"%red{Not a valid username! Use} %yellow{add [username]}")
      return
    }

    val userBestJson = request.getUserBestFromAPI(username)

    val topTenPlaysList = (userBestJson).extract[List[JObject]]

    var topTenPlays : Set[play] = Set.empty
    for (x <- topTenPlaysList.indices){
      val newBeatmapJson = request.getBeatmapFromAPI((topTenPlaysList(x)\"beatmap_id").extract[String].toInt)

      val newBeatmap : beatmap = beatmap(
        (newBeatmapJson\"title").extract[String],
        (newBeatmapJson\"artist").extract[String],
        (newBeatmapJson\"difficultyrating").extract[String].toDouble,
        (newBeatmapJson\"max_combo").extract[String].toInt,
      )

      mongo.createBeatmap(newBeatmap)

      topTenPlays += play(
        newBeatmap._id,
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