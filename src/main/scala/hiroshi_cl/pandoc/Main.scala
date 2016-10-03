package hiroshi_cl.pandoc

object Main {

  import PukiWiki._

  def main(args: Array[String]): Unit = {
    val str = scala.io.Source.stdin.mkString.trim
    println(str)
    println("# # #")
    parseAll(notLineBreaks, str) match {
      case Success(pandoc, _) => println("Success:\t" + pandoc)
      case NoSuccess(err, rest) => println("NoSuccess:\t" + err + "; " + rest.offset + ", " + rest.pos)
    }
  }
}
