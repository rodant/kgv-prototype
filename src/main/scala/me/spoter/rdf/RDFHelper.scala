package me.spoter.rdf

import java.net.URI

import scala.scalajs.js

/**
  * First draft of an abstraction over the RDFLib.
  */
// TODO: avoid dependency to JS, hint: use the RDF typescript defs.
object RDFHelper {
  val FOAF: js.Dynamic = RDFLib.Namespace("http://xmlns.com/foaf/0.1/")
  val GOOD_REL: js.Dynamic = RDFLib.Namespace("http://purl.org/goodrelations/v1#")
  val SCHEMA_ORG: js.Dynamic = RDFLib.Namespace("http://schema.org/")
  val LDP: js.Dynamic = RDFLib.Namespace("http://www.w3.org/ns/ldp#")

  private val store = RDFLib.graph()
  private val fetcher = new RDFFetcher(store)

  def load(sub: URI): js.Promise[js.Object] = fetcher.load(sub.toString)

  def get(sub: URI, prop: js.Dynamic): js.Dynamic = store.any(RDFLib.sym(sub.toString), prop)

  def getAll(sub: URI, prop: js.Dynamic): js.Dynamic = store.each(RDFLib.sym(sub.toString), prop)
}
