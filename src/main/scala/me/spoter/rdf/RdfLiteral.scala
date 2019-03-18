package me.spoter.rdf

import me.spoter.solid_libs.RDFLib

import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
  *
  */
case class RdfLiteral(value: String, lang: js.UndefOr[String] = js.undefined, typ: js.UndefOr[js.Dynamic] = js.undefined) {
  def toJSRdfLiteral: js.Dynamic = RDFLib.literal(value, lang, typ)
}

object RdfLiteral {
  def fromJSRflLiteral(literal: js.Dynamic): RdfLiteral = {
    val lang = if (literal.lang.asInstanceOf[UndefOr[_]] != js.undefined) UndefOr.any2undefOrA(literal.lang.toString) else js.undefined
    RdfLiteral(literal.value.toString, lang)
  }
}