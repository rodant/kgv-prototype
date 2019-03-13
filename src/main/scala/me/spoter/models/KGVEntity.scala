package me.spoter.models

import java.net.URI

trait KGVEntity {
  val uri: URI
  val title: String

  def withNewTitle(t: String): KGVEntity
}