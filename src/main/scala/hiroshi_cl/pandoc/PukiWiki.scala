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

  def pre: Parser[CodeBlock] = ((" " | "\t") ~> notLineBreaks) ^^
    (list => CodeBlock(Attr("", List.empty, List.empty), list.mkString("\n")))

  def uList(level: Int): Parser[BulletList] = uElement(level).+ ^^ BulletList

  def uElement(level: Int): Parser[ListItem] =
    exactLevel('-', level) ~> (not(notContainable) ~> not(lowerListElement(level)) ~> block).+ ^^ ListItem

  def oList(level: Int): Parser[OrderedList] = oElement(level).+ ^^
    (list => OrderedList(ListAttributes(level, DefaultStyle, DefaultDelim), list))

  def oElement(level: Int): Parser[ListItem] =
    exactLevel('+', level) ~> (not(notContainable) ~> not(lowerListElement(level)) ~> block).+ ^^ ListItem

  def bQuote(level: Int): Parser[BlockQuote] = bQuoteElement(level) ^^ BlockQuote

  def bQuoteElement(level: Int): Parser[List[Block]] =
    exactLevel('>', level) ~> (not(notContainable) ~> not(lowerBQuote(level)) ~> block).+ <~ exactLevel('<', level)

  def dList(level: Int): Parser[DefinitionList] = dElement(level).+ ^^ DefinitionList

  def dElement(level: Int): Parser[DefinitionItem] = exactLevel(':', level) ~ notLineBreaks ~ "|" ~ dDescription(level) ^^ {
    case _ ~ dt ~ _ ~ dd =>
      parseAll(inlines, dt) match {
        case Success(dtInlines, _) =>
          DefinitionItem(dtInlines, List(dd))
        case NoSuccess(_, _) =>
          throw null
      }
  }

  def dDescription(level: Int): Parser[Definition] =
    (not(notContainable) ~> not(lowerListElement(level)) ~> block).+ ^^ Definition

  def lowerListElement(maxLevel: Int): Parser[_] = maxLevel match {
    case 1 => "-[^\\-]".r | "+[^+]".r | ":[^:]".r
    case 2 => "-{1,2}[^\\-]".r | "+{1,2}[^+]".r | ":{1,2}[^:]".r
    case 3 => "-{1,2}[^\\-]".r | "+{1,2}[^+]".r | ":{1,2}[^:]".r | "---" | "+++" | ":::"
  }

  def lowerBQuote(maxLevel: Int): Parser[_] = maxLevel match {
    case 1 => "<" | ">[^>]".r
    case 2 => "<" | ">{1,2}[^>]".r
    case 3 => "<" | ">{1,2}[^>]".r | ">>>"
  }

  def exactLevel(symbol: Char, level: Int): Parser[_] = level match {
    case 1 => s"\\$symbol".r ~ not(s"[^\\$symbol]".r)
    case 2 => s"\\$symbol\\$symbol".r ~ not(s"[^\\$symbol]".r)
    case 3 => s"\\$symbol\\$symbol\\$symbol"
  }

  // PukiWiki の実装では対応しない閉じ bQuote は level 0 扱い
  def bQuote0: Parser[BlockQuote] = (exactLevel('<', 1) | exactLevel('<', 2) | exactLevel('<', 3)) ~>
    (not(notContainable) ~> not("<") ~> block).+ ^^ BlockQuote

  def table: Parser[Table] = ???

  def yTable: Parser[Table] = ???

  def blockPlugin: Parser[Block] = ???

  def paragraph: Parser[Para] = "~" ~> inlines ^^ Para

  def plain: Parser[Plain] = inlines ^^ Plain

  def notContainable: Parser[Block] = emptyLine | hRule | multiLinePlugin | heading

  def notInline: Parser[Block] = notContainable | align | pre |
    uList(1) | uList(2) | uList(3) | oList(1) | oList(2) | oList(3) | bQuote(1) | bQuote(2) | bQuote(3) | bQuote0 |
    dList(1) | dList(2) | dList(3) | table | yTable | blockPlugin |
    paragraph

  def block: Parser[Block] = comment | notInline | plain

  def body: Parser[Pandoc] = block.* ^^ (blocks => Pandoc(Meta(Map.empty), blocks))
}
