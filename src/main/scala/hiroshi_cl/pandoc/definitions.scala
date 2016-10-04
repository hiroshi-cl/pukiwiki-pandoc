package hiroshi_cl.pandoc

import scala.util.parsing.combinator._
import spandoc._

trait InlineParsers extends Parsers {
  def inlineMarkUps: Parser[List[Inline]]
}

trait BlockParsers extends Parsers {
  def block: Parser[Block]
}