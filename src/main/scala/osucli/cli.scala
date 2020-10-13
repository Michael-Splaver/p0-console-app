package osucli

import scala.io.StdIn
import scala.util.matching.Regex
import org.backuity.ansi.AnsiFormatter.FormattedHelper
import scala.io.Source
import net.liftweb.json._

class cli {
  //cmd, (args0,args1,args2,...)
  val commandArgPattern : Regex = "(\\w+)\\s*(.*)".r

  implicit val formats = DefaultFormats

  val config = parse(Source.fromFile("config.json").mkString)
  val api_key : Option[String] = (config\"api-key").extractOpt[String]

  def welcomeMessage() : Unit = {
    println(ansi"%bold{%magenta{✧･ﾟ: *✧･ﾟ:* %underline{Welcome to osu!cli} *:･ﾟ✧*:･ﾟ✧}}")
    println(ansi"%yellow{type help for a list of commands.}")
  }

  val commandsList = Map(
    "exit" -> "exit the osu!cli application",
    "help" -> "get the list of commands or specific command information"
  )

  def menu(): Unit = {
    welcomeMessage()
    var continueMenuLoop = true
    while(continueMenuLoop){
      StdIn.readLine() match {
        case commandArgPattern(cmd, arg) => {
          val command = cmd.toLowerCase
          var args = Array[String]()
          if (!arg.isEmpty) {args = arg.split(" ")}
          command match {
            case "help" => help(args)
            case "exit" => continueMenuLoop = false
            case "api" => println(api_key)
            case notFound => println(s"$notFound is not a recognized command.")
          }
        }
        case "" => //empty line -> do nothing
        case notRecognized
        => println(s"$notRecognized is not a recognized command format, use [command] {args}")
      }
    }
  }

  def help(args: Array[String]) : Unit = {
    args.length match {
      case 0 => {
        ansi"%cyan{The following are all available commands}"
        commandsList.foreach((element) => {
          println(ansi"%yellow{%bold{${element._1}}: ${element._2}}")
        })
      }
      case 1 => {
        val info = commandsList.get(args(0))
        if (info.isDefined){
          println(ansi"%yellow{%bold{${args(0)}}: ${info.get}}")
        }
        else{
          println(ansi"%red{That's not a recognized command!}")
        }
      }
      case _ => println(ansi"%red{Too many arguments! Use} %yellow{help} %red{or} %yellow{help [commandName]}")
    }
  }
}