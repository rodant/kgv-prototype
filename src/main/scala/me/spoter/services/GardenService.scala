package me.spoter.services

import java.net.URI

import me.spoter.models._
import me.spoter.solid_libs.{RDFHelper, RDFLib}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
  * RDF implementation of the garden service.
  */
object GardenService {
  private val spoterMeDirName = "spoterme"
  private val gardensDirName = "allotment_gardens"
  private val imagesDirName = "images"

  def fetchGarden(allotmentUri: URI, forceLoad: Boolean = false): Future[AllotmentGarden] = {
    val gardenDirUri = if (allotmentUri.getPath.endsWith("/")) allotmentUri else URI.create(s"$allotmentUri/")
    RDFHelper.loadEntity[Future[AllotmentGarden]](gardenDirUri, forceLoad) {
      val imageDir = RDFHelper.get(gardenDirUri, RDFHelper.SCHEMA_ORG("image"))
      val imageDirUri = URI.create(s"$gardenDirUri${imageDir.toString}").normalize()

      RDFHelper.listDir(imageDirUri, forceLoad)
        .recover { case _ => Seq() }
        .map[AllotmentGarden](populateLoadedGarden(gardenDirUri))
    }.flatten
  }

  def fetchGardensByWebId(webId: URI, forceLoad: Boolean = false): Future[Seq[AllotmentGarden]] = {
    for {
      gardensDirUri <- fetchGardensDirByWebId(webId)
      gardenUris <- RDFHelper.listDir(gardensDirUri, forceLoad).recover[Seq[URI]] {
        case e if e.getMessage.contains("Not Found") || e.getMessage.contains("404") => Seq()
        case e =>
          println(s"Got unexpected server error ${e.getMessage},\n when fetching the gardens dir for user $webId")
          Seq()
      }
      gardens <- Future.sequence(gardenUris.map(uri => GardenService.fetchGarden(uri)))
    } yield gardens
  }

  def fetchGardensDirByWebId(webId: URI): Future[URI] = RDFHelper.loadEntity(webId) {
    RDFHelper.get(webId, RDFHelper.PIM("storage")).value.toString
  }.map { s =>
    URI.create(s"$s$spoterMeDirName/$gardensDirName/")
  }

  def create(g: AllotmentGarden): Future[AllotmentGarden] = {
    val sts = gardenToSentences(g)
    val gardenIri = IRI(g.uri)

    val spoterResourceS = s"${g.uri.getScheme}://${g.uri.getHost}/$spoterMeDirName"
    val spoterResourceIri = IRI(spoterResourceS)
    for {
      _ <- RDFHelper.ensureContainerExists(spoterResourceIri)
      gardensResourceIri = IRI(s"$spoterResourceS/$gardensDirName")
      _ <- RDFHelper.ensureContainerExists(gardensResourceIri)
      canonicalGardenIri = gardenIri.removeTailingSlash
      baseIri = canonicalGardenIri.baseIRI
      uuid = canonicalGardenIri.lastPathComponent
      _ <- RDFHelper.createContainerResource(baseIri.innerUri, uuid)
      imagesIri = IRI(s"${gardenIri.toString}$imagesDirName")
      _ <- RDFHelper.ensureContainerExists(imagesIri)
      _ <- RDFHelper.addStatementsToWeb(sts)
    } yield g
  }

  def update(sub: IRI, field: RdfField, previous: RdfLiteral, next: RdfLiteral): Future[Unit] = {
    val subIriS = sub.toString
    val subSym = RDFLib.sym(subIriS)
    val docSym = RDFLib.sym(subIriS + ".meta")
    RDFHelper.updateStatement(previous, field.st(subSym, next, docSym))
  }

  private def gardenToSentences(g: AllotmentGarden): List[js.Dynamic] = {
    val gardenIri = IRI(g.uri)
    val gardenIriS = gardenIri.toString
    val sub = RDFLib.sym(gardenIriS)
    val doc = RDFLib.sym(gardenIriS + ".meta")
    List(
      RDFLib.st(sub, RDFHelper.RDF("type"), RDFHelper.PROD("Allotment_(gardening)"), doc),
      RDFLib.st(sub, RDFHelper.RDF("type"), RDFHelper.GOOD_REL("Individual"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("name"), g.title.toJSRdfLiteral, doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("description"), RDFLib.literal(g.description, "de"), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("image"), RDFLib.literal(s"$imagesDirName/"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("width"), RDFLib.literal("1"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("depth"), RDFLib.literal(g.area.a.toString), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("streetAddress"), RDFLib.literal(g.address.streetAndNumber, "de"), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("postalCode"), RDFLib.literal(g.address.zipCode.toString), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("addressRegion"), RDFLib.literal(g.address.region, "de"), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("addressCountry"), RDFLib.literal(g.address.country, "de"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("includes"), RDFLib.literal(g.bungalow.fold("")(_ => "Bungalow"), "de"), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("latitude"), RDFLib.literal(g.location.latitude.toString, typ = RDFHelper.XMLS("float")), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("longitude"), RDFLib.literal(g.location.longitude.toString, typ = RDFHelper.XMLS("float")), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("condition"), RDFLib.literal(g.condition.toString, "de"), doc)
    )
  }

  private def bestChoiceFor(sub: URI, prop: js.Dynamic): Option[RdfLiteral] = {
    val sts = RDFHelper.statementsMatching(Some(sub), Some(prop), None, None)
    sts.find(_.why.value.toString.endsWith("/.meta"))
      .orElse(sts.headOption)
      .map(st => RdfLiteral.fromJSRflLiteral(st.`object`))
  }

  private def populateLoadedGarden(allotmentUri: URI)(imageUris: Seq[URI]): AllotmentGarden = {
    val allotmentTitleOpt = bestChoiceFor(allotmentUri, RDFHelper.GOOD_REL("name"))
    allotmentTitleOpt
      .fold(AllotmentGarden(uri = allotmentUri, title = RdfLiteral(s"Dieser Garten ist fehlerhaft, id: $allotmentUri"))) { title =>
        val allotmentDesc = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("description"))

        val latitude = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("latitude"))
        val longitude = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("longitude"))
        val latStr = latitude.toString
        val lngStr = longitude.toString
        val location = Location(latStr.toDouble, lngStr.toDouble)

        val streetAddress = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("streetAddress"))
        val postalCode = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("postalCode"))
        val addressRegion = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("addressRegion"))
        val addressCountry = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("addressCountry"))
        val address = Address(streetAddress.toString, postalCode.toString.toInt, addressRegion.toString, addressCountry.toString)

        val includes = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("includes"))
        val condition = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("condition"))

        val width = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("width"))
        val depth = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("depth"))

        val garden = AllotmentGarden(
          uri = allotmentUri,
          title = title,
          description = allotmentDesc.toString,
          location = location,
          address = address,
          bungalow = if (!includes.toString.isEmpty) Some(Bungalow()) else None,
          area = Area(width.toString.toDouble * depth.toString.toDouble),
          condition = AllotmentCondition.namesToValuesMap.getOrElse(condition.toString, AllotmentCondition.Undefined)
        )
        if (imageUris.nonEmpty) garden.copy(images = imageUris) else garden
      }
  }

  case class RdfLiteral(value: String, lang: js.UndefOr[String] = js.undefined, typ: js.UndefOr[js.Dynamic] = js.undefined) {
    def toJSRdfLiteral: js.Dynamic = RDFLib.literal(value, lang, typ)
  }

  object RdfLiteral {
    def fromJSRflLiteral(literal: js.Dynamic): RdfLiteral = {
      val lang = if (literal.lang.asInstanceOf[UndefOr[_]] != js.undefined) UndefOr.any2undefOrA(literal.lang.toString) else js.undefined
      RdfLiteral(literal.value.toString, lang)
    }
  }

  sealed trait RdfField {
    val predicate: js.Dynamic

    def st(sub: js.Dynamic, literal: RdfLiteral, doc: js.Dynamic): js.Dynamic
  }

  case object Name extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.GOOD_REL("name")

    override def st(sub: js.Dynamic, literal: RdfLiteral, doc: js.Dynamic): js.Dynamic =
      RDFLib.st(sub, predicate, literal.toJSRdfLiteral, doc)
  }

}
