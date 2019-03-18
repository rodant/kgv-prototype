package me.spoter.models

import java.net.URI

import me.spoter.rdf.RdfLiteral

trait KGVEntity {
  val uri: URI
  val title: RdfLiteral

  def withNewTitle(t: RdfLiteral): KGVEntity
}