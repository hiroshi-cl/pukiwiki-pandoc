package hiroshi_cl.pandoc

import org.scalatest._
import spandoc._

class Spec extends FlatSpec with Matchers {

  import MockPukiWikiParsers._

  "block" can "parse string" in {
    parseAll(block, "hello") match {
      case Success(pandoc, _) => pandoc should === (Plain(List(Str("hello"))))
    }
  }
}
