package me.spoter.services

import java.net.URI

import me.spoter.models._
import me.spoter.rdf.RdfLiteral
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

  private def populateLoadedGarden(allotmentUri: URI)(imageUris: Seq[URI]): AllotmentGarden = {
    val allotmentTitleOpt = bestChoiceFor(allotmentUri, Name.predicate)
    allotmentTitleOpt
      .fold(AllotmentGarden(uri = allotmentUri, title = RdfLiteral(s"Dieser Garten ist fehlerhaft, id: $allotmentUri"))) { title =>
        val allotmentDesc = bestChoiceFor(allotmentUri, Description.predicate).getOrElse(Description.default)

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
          description = allotmentDesc,
          location = location,
          address = address,
          bungalow = if (!includes.toString.isEmpty) Some(Bungalow()) else None,
          area = Area(width.toString.toDouble * depth.toString.toDouble),
          condition = AllotmentCondition.namesToValuesMap.getOrElse(condition.toString, AllotmentCondition.Undefined)
        )
        if (imageUris.nonEmpty) garden.copy(images = imageUris) else garden
      }
  }

  private def bestChoiceFor(sub: URI, prop: js.Dynamic): Option[RdfLiteral] = {
    val sts = RDFHelper.statementsMatching(Some(sub), Some(prop), None, None)
    sts.find(_.why.value.toString.endsWith("/.meta"))
      .orElse(sts.headOption)
      .map(st => RdfLiteral.fromJSRflLiteral(st.`object`))
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

  private def gardenToSentences(g: AllotmentGarden): List[js.Dynamic] = {
    val gardenIri = IRI(g.uri)
    val gardenIriS = gardenIri.toString
    val sub = RDFLib.sym(gardenIriS)
    val doc = RDFLib.sym(gardenIriS + ".meta")
    List(
      RDFLib.st(sub, RDFHelper.RDF("type"), RDFHelper.PROD("Allotment_(gardening)"), doc),
      RDFLib.st(sub, RDFHelper.RDF("type"), RDFHelper.GOOD_REL("Individual"), doc),
      Name.st(sub, g.title, doc),
      Description.st(sub, g.description, doc),
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

  def update(sub: IRI, field: RdfField, previous: RdfLiteral, next: RdfLiteral): Future[Unit] = {
    val subIriS = sub.toString
    val subSym = RDFLib.sym(subIriS)
    val docSym = RDFLib.sym(subIriS + ".meta")
    RDFHelper.updateStatement(previous, field.st(subSym, next, docSym))
  }

  case object Name extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.GOOD_REL("name")

    override val default: RdfLiteral = RdfLiteral("", UndefOr.any2undefOrA("de"))
  }

  case object Description extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.GOOD_REL("description")

    override val default: RdfLiteral = RdfLiteral("", UndefOr.any2undefOrA("de"))
  }

}
