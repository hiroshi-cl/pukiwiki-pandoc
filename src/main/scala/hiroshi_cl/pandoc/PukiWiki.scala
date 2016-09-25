package hiroshi_cl.pandoc

import scala.util.parsing.combinator._
import spandoc._

object PukiWiki extends RegexParsers {
  def comment: Parser[Null.type] = "//" ~ notLineBreaks ~ lineBreaks ^^^ Null

  def inlines: Parser[List[Inline]] = ???

  def align: Parser[Div] = ("LEFT" | "RIGHT" | "CENTER") ~ ":" ~ inlines ^? {
    case a ~ _ ~ c =>
      Div(Attr("", List("text-align-" + a.toLowerCase), List("style" -> ("text-align: " + a.toLowerCase))), List(Plain(c)))
  }

  def notLineBreaks = "[^\r\n]*?".r

  def lineBreaks = "\r\n" | "\r" | "\n"

  def emptyLine: Parser[Null.type] = lineBreaks ^^^ Null

  def hRule: Parser[HorizontalRule.type] = "----" ~ notLineBreaks ~ lineBreaks ^^^ HorizontalRule

  def multiLinePlugin: Parser[Div] = ???

  def heading: Parser[Header] = "\\*{0,3}".r ~ notLineBreaks ~ ("\\[#" ~> "[A-Za-z][\\w-]+" <~ "\\]").? ~ notLineBreaks <~ lineBreaks ^^ {
    case level ~ title1 ~ anchorOpt ~ title2 =>
      parseAll(inlines, title1 + title2) match {
        case Success(title, _) =>
          Header(level.length, Attr(anchorOpt.getOrElse(""), List.empty, List.empty), title)
        case NoSuccess(_, _) =>
          throw null
      }
  }

  def pre: Parser[CodeBlock] = ((" " | "\t") ~> notLineBreaks) ^^ (list => CodeBlock(Attr("", List.empty, List.empty), list.mkString("\n")))

  def uList: Parser[BulletList] = ???

  def oList: Parser[OrderedList] = ???

  def bQuote: Parser[BlockQuote] = ???

  def dList: Parser[DefinitionList] = ???

  def table: Parser[Table] = ???

  def yTable: Parser[Table] = ???

  def blockPlugin: Parser[Block] = ???

  def paragraph: Parser[Para] = "~" ~> inlines ^^ Para

  def plain: Parser[Plain] = inlines ^^ Plain

  def notContainable: Parser[Block] = emptyLine | hRule | multiLinePlugin | heading

  def notInline: Parser[Block] = notContainable | align | pre |
    uList | oList | bQuote |
    dList | table | yTable | blockPlugin |
    paragraph

  def block: Parser[Block] = comment | notInline | plain

  def body: Parser[Pandoc] = block.* ^^ (blocks => Pandoc(Meta(Map.empty), blocks))
}
