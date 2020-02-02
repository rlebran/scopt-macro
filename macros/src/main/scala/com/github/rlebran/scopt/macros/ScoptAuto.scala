package com.github.rlebran.scopt.macros

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros

object ScoptAuto {

  @compileTimeOnly("enable macro paradise to expand macro annotations")
  class autoParser extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro ScoptAutoParsing.impl
  }

  @compileTimeOnly("enable macro paradise to expand macro annotations")
  class configAutoParser(optList: String*) extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro ScoptAutoParsing.impl
  }

}
