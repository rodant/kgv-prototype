package me.spoter.rdf

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  *
  */
@js.native
@JSImport("rdflib", JSImport.Default)
object RDFLib extends js.Object {
  def Namespace(uri: String): js.Dynamic = js.native

  def graph(): js.Dynamic = js.native

  def sym(subject: String): js.Dynamic = js.native
}

@js.native
@JSImport("rdflib", "Fetcher")
class RDFFetcher(store: js.Dynamic) extends js.Object {
  def load(subject: String): js.Promise[js.Object] = js.native
}
