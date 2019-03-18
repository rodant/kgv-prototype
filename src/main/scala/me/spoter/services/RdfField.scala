package me.spoter.services

import me.spoter.rdf.RdfLiteral

import scala.scalajs.js

/**
  *
  */
trait RdfField {
  val predicate: js.Dynamic

  def st(sub: js.Dynamic, literal: RdfLiteral, doc: js.Dynamic): js.Dynamic
}
