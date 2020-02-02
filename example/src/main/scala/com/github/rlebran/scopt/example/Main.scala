package com.github.rlebran.scopt.example

object Main {
  def main(args: Array[String]): Unit = {
    /*
     test it with:
          --val1 "value" --test2 130 --opt3 "optStr"
          or
          --val1 "value" --test2 130
     */
    //    val res = Test("", 0, None).parse(args)
    //    println(res)

    /*
     test it with:
          --a 0.1 --map key1=val1,key2=val2
          or
          --a 0.1 --_opt_Map key1=val1,key2=val2
          or
          --a 0.1
     */
    //    val res2 = AnotherOne(0, Map.empty).parse(args)
    //    println(res2)

    /*
     test it with:
          --a 0.1 --map key1=val1,key2=val2
          or no parameters
     */
    //    val res3 = AnotherOneParam(0, Map.empty).parse(args)
    //    println(res3)
  }
}
