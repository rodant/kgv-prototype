package me.spoter.solid_libs

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

  def createContainer(parentURI: String, folderName: String, data: js.UndefOr[String]): js.Promise[js.Object] = js.native
}

@js.native
@JSImport("rdflib", "UpdateManager")
class RDFUpdateManager(store: js.Dynamic) extends js.Object {
  def put(doc: js.Dynamic, data: js.Array[js.Dynamic], contentType: String, callback: js.Function): js.Promise[js.Object] = js.native
}