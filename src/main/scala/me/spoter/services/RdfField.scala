package me.spoter.services

import me.spoter.rdf.RdfLiteral
import me.spoter.solid_libs.RDFLib

import scala.scalajs.js

/**
  *
  */
trait RdfField {
  val predicate: js.Dynamic

  val default: RdfLiteral

  def st(sub: js.Dynamic, literal: RdfLiteral, doc: js.Dynamic): js.Dynamic = RDFLib.st(sub, predicate, literal.toJSRdfLiteral, doc)
}
