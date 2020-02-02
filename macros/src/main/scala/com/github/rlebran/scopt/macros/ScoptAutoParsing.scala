package com.github.rlebran.scopt.macros

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

private[macros] object ScoptAutoParsing {

  def impl(ctx: blackbox.Context)(annottees: ctx.Expr[Any]*): ctx.Expr[Any] = {
    import ctx.universe._

    val optList = extractArgs(ctx)

    annottees.map(_.tree) match {
      case q"$mods class $name[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: Nil =>
        val opts = paramss.flatten.map {
          case q"${_} val $name: $typeOpt = ${_}" =>
            val paramName = Literal(Constant(name.toString()))
            typeOpt match {
              case tq"Option[$tpe]" =>
                q"""
                 opt[$tpe]($paramName)
                   .optional
                   .action((x, c) => c.copy($name = Some(x)))
               """
              case _ if name.toString().startsWith("_opt_") =>
                val c = name.toString().stripPrefix("_opt_").toCharArray
                c(0) = Character.toLowerCase(c(0))
                val normName = Literal(Constant(new String(c)))
                q"""
                 opt[$typeOpt]($normName)
                   .optional
                   .action((x, c) => c.copy($name = x))
                 opt[$typeOpt]($paramName)
                   .optional
                   .action((x, c) => c.copy($name = x))
               """
              case _ if optList.contains(name.toString()) =>
                q"""
                 opt[$typeOpt]($paramName)
                   .optional
                   .action((x, c) => c.copy($name = x))
               """
              case _ =>
                q"""
                 opt[$typeOpt]($paramName)
                   .required
                   .action((x, c) => c.copy($name = x))
               """
            }
        }
        val termName = Literal(Constant(name.toString().toLowerCase))
        val optParser = q"lazy val parser: OptionParser[$name] = new OptionParser[$name]($termName){ ..$opts }"

        val generatedCode =
          q"""
           $mods class $name[..$tparams] $ctorMods(...$paramss) extends {
             ..$earlydefns
           } with ..$parents { $self =>
             ..$stats
             import scopt._
             ..$optParser
             def parse(args: Seq[String]): Option[$name] = { parser.parse(args, this) }
           }
         """
        ctx.Expr[Any](generatedCode)
    }
  }

  private def extractArgs(ctx: blackbox.Context): List[String] = {
    import ctx.universe._

    ctx.macroApplication match {
      case Apply(Select(Apply(_, xs: List[_]), _), _) => xs.flatMap { case t: Tree =>
        t.collect {
          case Literal(Constant(arg@(_: String))) => arg.toString
        }
      }
      case _ => Nil
    }
  }

}
