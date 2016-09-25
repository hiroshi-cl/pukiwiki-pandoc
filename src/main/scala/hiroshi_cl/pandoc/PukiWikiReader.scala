//package hiroshi_cl.pandoc
//
//import spandoc.{Block, Pandoc}
//
//object PukiWikiReader {
//  def lcnt(cs: Array[Char], c: Char, offset: Int): Int = {
//    var ret = 0
//    var i = offset
//    while (i < cs.length && cs(i) == c) {
//      i += 1
//      ret += 1
//    }
//    ret
//  }
//}
//
//sealed trait Align
//
//case object LEFT extends Align
//
//case object RIGHT extends Align
//
//case object CENTER extends Align
//
//sealed abstract class TableMode
//
//case object Head extends TableMode
//
//case object Body extends TableMode
//
//case object Foot extends TableMode
//
//case object Config extends TableMode
//
//object TableMode {
//  def apply(column: String): TableMode = column match {
//    case "cb" =>
//      Config
//    case "hb" =>
//      Head
//    case "fb" =>
//      Foot
//    case "b" =>
//      Body
//    case _ =>
//      throw null
//  }
//}
//
//class PukiWikiReader(source: String) {
//
//  import PukiWikiReader._
//
//  val sb = new StringBuilder()
//  val cs = (source + '\n').toCharArray
//  var c = 0
//
//  def parse(): spandoc.Pandoc = {
//    var blocks = List.empty[spandoc.Block]
//    while (c < cs.length) {
//      while (comment()) {
//
//      }
//      blocks +:= block()
//    }
//    spandoc.Pandoc(spandoc.Meta(Map.empty), blocks)
//  }
//
//  def comment(): Boolean = {
//    if (cs(c) != '/' || cs(c + 1) != '/')
//      false
//    else {
//      readLine()
//      true
//    }
//  }
//
//  def block(): spandoc.Block = {
//    val t = c
//    val s = nonParagraphBlock()
//    if (s == null) {
//      c = t
//      paragraph()
//    } else
//      s
//  }
//
//  def nonParagraphBlock(): spandoc.Block =
//    cs(c) match {
//      case '\n' =>
//        empty()
//      case '>' | '<' =>
//        quote()
//      case '-' =>
//        itemize()
//      case '+' =>
//        enumerate()
//      case '*' =>
//        section()
//      case '#' =>
//        blockPlugin()
//      case '|' =>
//        table()
//      case ':' =>
//        description()
//      case ' ' =>
//        verbatim()
//      case _ =>
//        null
//    }
//
//  def empty(): spandoc.Block = {
//    c += 1
//    spandoc.Null
//  }
//
//  def rawParagraph(): List[spandoc.Inline] = {
//    var inlines = List.empty[spandoc.Inline]
//    var b = true
//    while (b) {
//      if (cs(c) == '~')
//        c += 1
//      while (comment()) {}
//      val line = readLine()
//      if (line.endsWith("~")) {
//        inlines +:= inline(line.substring(0, line.length() - 1))
//        inlines +:= spandoc.LineBreak
//      } else {
//        inlines +:= inline(line))
//        inlines +:= spandoc.SoftBreak
//        val t = c
//        val s = nonParagraphBlock()
//        c = t
//        if (s != null)
//          b = false
//      }
//    }
//    inlines
//  }
//
//  def paragraph(): spandoc.Block = spandoc.Para(rawParagraph())
//
//  def quote(): spandoc.Block = ???
//
//  def itemize(): spandoc.Block = {
//    val level = lcnt(cs, '-', c)
//    val container = new StringBuilder()
//    while (lcnt(cs, '-', c) == level) {
//      val item = new StringBuilder()
//      c += level
//      if (cs(c) == '\n')
//        item.append(block())
//      else
//        item.append(rawParagraph())
//      while (lcnt(cs, '-', c) > level)
//        item.append(itemize())
//      container.append(itemizeItem(item.toString()))
//    }
//    itemizeContainer(container.toString())
//  }
//
//  def enumerate(): spandoc.Block = {
//    val level = lcnt(cs, '+', c)
//    val container = new StringBuilder()
//    while (lcnt(cs, '+', c) == level) {
//      val item = new StringBuilder()
//      c += level
//      if (cs(c) == '\n')
//        item.append(block())
//      else
//        item.append(rawParagraph())
//      while (lcnt(cs, '+', c) > level)
//        item.append(enumerate())
//      container.append(enumerateItem(item.toString()))
//    }
//    enumerateContainer(container.toString())
//  }
//
//  def section(): spandoc.Block = {
//    c += 3
//    subsubsection(readLine().replaceAll("\\[.+?\\]", "").trim())
//  }
//
//  def blockPlugin(): spandoc.Block = ???
//
//  def description(): spandoc.Block = {
//    val level = lcnt(cs, ':', c)
//    val container = new StringBuilder()
//    while (lcnt(cs, ':', c) == level) {
//      {
//        c += level
//        val caption = new StringBuilder()
//        while (cs(c) != '|') {
//          caption.append(cs(c))
//          c += 1
//          if (cs(c) == '\n')
//            return null
//        }
//        container.append(descriptionCaption(inline(caption.toString())))
//        c += 1
//      }
//      {
//        val item = new StringBuilder()
//        if (cs(c) == '\n')
//          item.append(block())
//        else
//          item.append(rawParagraph())
//        while (lcnt(cs, ':', c) > level)
//          item.append(description())
//        container.append(descriptionItem(item.toString()))
//      }
//    }
//    descriptionContainer(container.toString());
//  }
//
//  def table(): spandoc.Block = {
//    val first = readLine()
//    if (!"cfh|".contains(first.substring(first.length() - 1)))
//      return null;
//    var ss = (first + "b").substring(1).split("\\|")
//    val columns = ss.length - 1
//    val head = new StringBuilder()
//    val body = new StringBuilder()
//    val foot = new StringBuilder()
//    var line = first
//    var b = true
//    while (b) {
//      val mode = TableMode(ss(columns))
//      if (mode == Config)
//        throw null
//      val linesb = new StringBuilder()
//      var i = 0
//      while (i < columns) {
//        if (ss(i).startsWith("~"))
//          linesb.append(wrapTH(ss(i).substring(1)))
//        else
//          linesb.append(wrapTC(ss(i)))
//        i += 1
//      }
//
//      val wrappedLine = wrapTR(linesb.toString())
//      mode match {
//        case Head =>
//          head.append(wrappedLine)
//        case Body =>
//          body.append(wrappedLine)
//        case Foot =>
//          foot.append(wrappedLine)
//      }
//      var t = c
//      line = readLine()
//      if (line.isEmpty || !"cfh|".contains(line.substring(line.length() - 1))) {
//        c = t
//        b = false
//      }
//      ss = (line + "b").substring(1).split("\\|")
//      if (ss.length - 1 != columns) {
//        c = t
//        b = false
//      }
//    }
//    wrapTable(
//      wrapTHead(head.toString()) + wrapTBody(body.toString()) + wrapTFoot(foot.toString()),
//      columns)
//  }
//
//  def verbatim(): spandoc.Block = {
//    val sb = new StringBuilder()
//    while (cs(c) == ' ') {
//      c += 1
//      if (sb.nonEmpty)
//        sb.append('\n')
//      sb.append(readLine())
//    }
//    verbatimContainer(sb.toString)
//  }
//
//  def inline(s: String): String = {
//    if (s.contains(":")) {
//      val ss = s.split(":", 2)
//      ss(0) match {
//        case "LEFT" => align(LEFT, inline(ss(1)))
//        case "RIGHT" => align(RIGHT, inline(ss(1)))
//        case "CENTER" => align(CENTER, inline(ss(1)))
//      }
//    }
//    var i = 0
//    while (i < s.length()) {
//      if (s.startsWith("%%%", i) && s.substring(i + 3).contains("%%%")) {
//        val ss = s.substring(i + 3).split("%%%", 2)
//        return plain(s.substring(0, i)) + underbar(inline(ss(0))) + inline(ss(1))
//      }
//      if (s.startsWith("%%", i) && s.substring(i + 2).contains("%%")) {
//        val ss = s.substring(i + 2).split("%%", 2)
//        return plain(s.substring(0, i)) + strike(inline(ss(0))) + inline(ss(1))
//      }
//      if (s.startsWith("'''", i) && s.substring(i + 3).contains("'''")) {
//        val ss = s.substring(i + 3).split("'''", 2)
//        return plain(s.substring(0, i)) + italic(inline(ss(0))) + inline(ss(1))
//      }
//      if (s.startsWith("''", i) && s.substring(i + 2).contains("''")) {
//        val ss = s.substring(i + 2).split("''", 2)
//        return plain(s.substring(0, i)) + strong(inline(ss(0))) + inline(ss(1))
//      }
//      if (s.startsWith("((", i) && s.substring(i + 2).contains("))")) {
//        val ss = s.substring(i + 2).split("\\)\\)", 2)
//        return plain(s.substring(0, i)) + annotation(inline(ss(0))) + inline(ss(1))
//      }
//      if (s.startsWith("$", i) && s.substring(i + 1).contains("$")) {
//        val ss = s.substring(i + 1).split("\\$", 2)
//        return plain(s.substring(0, i)) + expression(ss(0)) + inline(ss(1))
//      }
//      if (s.startsWith("&", i) && s.substring(i + 1).contains(";")) {
//        val ss = s.substring(i + 1).split(";", 2)
//        return plain(s.substring(0, i)) + inlinePlugin(ss(0)) + inline(ss(1))
//      }
//      i += 1
//    }
//    plain(s)
//  }
//
//  def plain(s: String): String = ???
//
//  def align(a: Align, s: String): String = ???
//
//  def strike(s: String): String = ???
//
//  def underbar(s: String): String = ???
//
//  def annotation(s: String): String = ???
//
//  def strong(s: String): String = ???
//
//  def italic(s: String): String = ???
//
//  def expression(s: String): String = ???
//
//  def inlinePlugin(s: String): String = ???
//
//  def readLine(): String = {
//    val sb = new StringBuilder()
//    while (cs(c) != '\n') {
//      sb.append(cs(c))
//      c += 1
//    }
//    c += 1
//    sb.toString()
//  }
//}
