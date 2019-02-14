package me.spoter.rdf

import java.net.URI

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
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
  val VCARD: js.Dynamic = RDFLib.Namespace("http://www.w3.org/2006/vcard/ns#")

  private val store = RDFLib.graph()
  private val fetcher = new RDFFetcher(store)

  private def load(sub: URI): Future[js.Object] = fetcher.load(sub.toString).toFuture

  def loadEntity[A](sub: URI)(b: => A): Future[A] = load(sub).map(_ => b)

  def listDir(dirUri: URI): Future[Seq[URI]] = RDFHelper.loadEntity[Seq[URI]](dirUri) {
    val filesNodes = RDFHelper.getAll(dirUri, RDFHelper.LDP("contains")).asInstanceOf[js.Array[js.Dynamic]]
    filesNodes.map(f => new URI(f.value.toString))
  }

  def get(sub: URI, prop: js.Dynamic): js.Dynamic = store.any(RDFLib.sym(sub.toString), prop)

  def getAll(sub: URI, prop: js.Dynamic): js.Dynamic = store.each(RDFLib.sym(sub.toString), prop)
}
