package me.spoter.rdf

import java.net.URI

import scala.scalajs.js

/**
  * First draft of an abstraction over the RDFLib.
  */
// TODO: avoid dependency to JS, hint: use the RDF typescript defs.
object RDFHelper {
  val GOOD_REL: js.Dynamic = RDFLib.Namespace("http://purl.org/goodrelations/v1#")
  private val store = RDFLib.graph()
  private val fetcher = new RDFFetcher(store)

  def load(sub: URI): js.Promise[js.Object] = fetcher.load(sub.toString)

  def get(sub: URI, prop: js.Dynamic): Any = store.any(RDFLib.sym(sub.toString), prop)
}
