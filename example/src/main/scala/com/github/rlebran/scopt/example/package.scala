package com.github.rlebran.scopt

import com.github.rlebran.scopt.macros.ScoptAuto.{autoParser, configAutoParser}

package object example {

  @autoParser
  case class Test(val1: String, test2: Int, opt3: Option[String])

  @autoParser
  case class AnotherOne(a: Double, _opt_Map: Map[String, String])

  @configAutoParser("a", "map")
  case class AnotherOneParam(a: Double, map: Map[String, String])

}
