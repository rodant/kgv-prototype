package me.spoter.solid_libs

import java.net.URI

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
  * First draft of an abstraction over the RDFLib.
  */
// TODO: avoid dependency to JS, hint: use the RDF typescript defs.
object RDFHelper {
  val RDF: js.Dynamic = RDFLib.Namespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#")
  val FOAF: js.Dynamic = RDFLib.Namespace("http://xmlns.com/foaf/0.1/")
  val GOOD_REL: js.Dynamic = RDFLib.Namespace("http://purl.org/goodrelations/v1#")
  val PROD: js.Dynamic = RDFLib.Namespace("http://www.productontology.org/id/")
  val SCHEMA_ORG: js.Dynamic = RDFLib.Namespace("http://schema.org/")
  val LDP: js.Dynamic = RDFLib.Namespace("http://www.w3.org/ns/ldp#")
  val VCARD: js.Dynamic = RDFLib.Namespace("http://www.w3.org/2006/vcard/ns#")
  val PIM: js.Dynamic = RDFLib.Namespace("http://www.w3.org/ns/pim/space#")

  private val store = RDFLib.graph()
  private val fetcher = new RDFFetcher(store)
  private val updateManager = new RDFUpdateManager(store)

  private def load(sub: URI): Future[js.Object] = fetcher.load(sub.toString).toFuture

  def loadEntity[A](sub: URI)(b: => A): Future[A] = load(sub).map(_ => b)

  def listDir(dirUri: URI): Future[Seq[URI]] = RDFHelper.loadEntity[Seq[URI]](dirUri) {
    val filesNodes = RDFHelper.getAll(dirUri, RDFHelper.LDP("contains")).asInstanceOf[js.Array[js.Dynamic]]
    filesNodes.map(f => new URI(f.value.toString))
  }

  private def getAll(sub: URI, prop: js.Dynamic): js.Dynamic = store.each(RDFLib.sym(sub.toString), prop)

  def get(sub: URI, prop: js.Dynamic): js.Dynamic = store.any(RDFLib.sym(sub.toString), prop)

  def statementsMatching(sub: Option[URI], prop: Option[js.Dynamic], obj: Option[URI], doc: Option[URI]): Seq[js.Dynamic] = {
    import js.JSConverters._
    val subNode = sub.map(s => RDFLib.sym(s.toString)).orUndefined
    val objNode = obj.map(o => RDFLib.sym(o.toString)).orUndefined
    val propNode = prop.orUndefined
    val docNode = doc.map(d => RDFLib.sym(d.toString)).orUndefined
    store.`match`(subNode, propNode, objNode, docNode).asInstanceOf[js.Array[js.Dynamic]]
  }

  def createFileResource(sub: URI, data: Seq[js.Dynamic], callback: js.Function): Future[js.Object] = {
    updateManager.put(RDFLib.sym(sub.toString), data.toJSArray, "text/turtle", callback).toFuture
  }

  /**
    * don't use @metaString, there is a bug in the rdflib: https://github.com/linkeddata/rdflib.js/issues/266
    */
  def createContainerResource(parentUri: URI, containerName: String, metaString: Option[String] = None): Future[js.Object] = {
    fetcher.createContainer(parentUri.toString, containerName, metaString.orUndefined).toFuture
  }

  def addStatementToWeb(st: js.Dynamic): Future[Unit] = {
    val p = Promise[Unit]()
    val callback = (uri: js.UndefOr[String], success: Boolean, error: js.UndefOr[String]) => {
      if (success) {
        p.success()
      } else {
        p.failure(new Exception(error.get))
      }
    }
    updateManager.update(js.undefined, js.Array(st), callback)
    p.future
  }
}
