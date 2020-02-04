package com.github.rlebran.scopt.macros

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

private[macros] object ScoptAutoParsing {

  def impl(ctx: blackbox.Context)(annottees: ctx.Expr[Any]*): ctx.Expr[Any] = {
    ctx.Expr[Any](treeGeneration(ctx)(annottees: _*))
  }

  def implDebug(ctx: blackbox.Context)(
      annottees: ctx.Expr[Any]*): ctx.Expr[Any] = {
    val tree = treeGeneration(ctx)(annottees: _*)
    println(tree)
    ctx.Expr[Any](tree)
  }

  private def treeGeneration(ctx: blackbox.Context)(
      annottees: ctx.Expr[Any]*): ctx.universe.Tree = {
    import ctx.universe._

    val specificParams = extractArgs(ctx).flatMap { paramAndMod =>
      val splitted = paramAndMod.split(":", 2)
      for {
        param <- splitted.headOption
        mods <- splitted.lastOption
      } yield param -> mods.split(",").toSet
    }.toMap

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
              case _ if specificParams.get(name.toString()).isDefined =>
                val modifiers =
                  specificParams(name.toString()).toSeq.sorted.toList
                modifiers match {
                  case opt :: unb :: Nil
                      if (opt == "optional" || opt == "opt") && (unb == "unbounded" || unb == "unb") =>
                    q"""
                       opt[$typeOpt]($paramName)
                         .unbounded
                         .optional
                         .action((x, c) => c.copy($name = x))
                     """
                  case opt :: Nil if opt == "optional" || opt == "opt" =>
                    q"""
                       opt[$typeOpt]($paramName)
                         .optional
                         .action((x, c) => c.copy($name = x))
                     """
                  case unb :: Nil if unb == "unbounded" || unb == "unb" =>
                    q"""
                       opt[$typeOpt]($paramName)
                         .unbounded
                         .action((x, c) => c.copy($name = x))
                     """
                  case _ =>
                    q"""
                       opt[$typeOpt]($paramName)
                         .optional
                         .action((x, c) => c.copy($name = x))
                     """
                }
              case _ =>
                q"""
                 opt[$typeOpt]($paramName)
                   .required
                   .action((x, c) => c.copy($name = x))
               """
            }
        }
        val termName = Literal(Constant(name.toString().toLowerCase))
        val optParser =
          q"lazy val parser: OptionParser[$name] = new OptionParser[$name]($termName){ ..$opts }"

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
    }
  }

  private def extractArgs(ctx: blackbox.Context): List[String] = {
    import ctx.universe._

    ctx.macroApplication match {
      case Apply(Select(Apply(_, xs: List[_]), _), _) =>
        xs.flatMap {
          case t: Tree =>
            t.collect {
              case Literal(Constant(arg @ (_: String))) => arg.toString
            }
        }
      case _ => Nil
    }
  }

}
