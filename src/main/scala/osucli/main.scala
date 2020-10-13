package osucli

object main extends App {
  new cli().menu()
}
/*
  A Scala CLI (Command Line Interface) application.
  Data should be parsed from a CSV or JSON file and persisted to MongoDB.

  - pull data from osu! (the game) and persist to mongodb
    - requests (getuser, getuserstats, map stats)
    - hide credentials (from repo)
    - compare to other users
    - top 10
    - stat analysis

  - components
    - cli interaction
    - database interaction
    - http interaction
    - main logic
 */
