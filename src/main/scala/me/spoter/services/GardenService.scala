package me.spoter.services

import java.net.URI

import me.spoter.models._
import me.spoter.solid_libs.{RDFHelper, RDFLib}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * RDF implementation of the garden service.
  */
object GardenService {
  private val spotermeDirName = "spoterme"
  private val gardensDirName = "allotment_gardens"

  def fetchGarden(allotmentUri: URI): Future[AllotmentGarden] =
    RDFHelper.loadEntity[Future[AllotmentGarden]](allotmentUri) {
      val imageDir = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("image"))
      val imageDirUri = URI.create(s"${allotmentUri.toString}/${imageDir.toString}").normalize()

      RDFHelper.listDir(imageDirUri)
        .recover { case _ => Seq() }
        .map[AllotmentGarden](populateLoadedGarden(allotmentUri))
    }.flatten

  def fetchGardensByWebId(webId: URI): Future[Seq[AllotmentGarden]] = {
    for {
      gardensDirUri <- fetchGardensDirByWebId(webId)
      gardenUris <- RDFHelper.listDir(gardensDirUri).recover[Seq[URI]] {
        case e if e.getMessage.contains("Not Found") || e.getMessage.contains("404") => Seq()
        case e =>
          println(s"Got unexpected server error ${e.getMessage},\n when fetching the gardens dir for user $webId")
          Seq()
      }
      gardens <- Future.sequence(gardenUris.map(GardenService.fetchGarden))
    } yield gardens
  }

  def fetchGardensDirByWebId(webId: URI): Future[URI] = RDFHelper.loadEntity(webId) {
    RDFHelper.get(webId, RDFHelper.PIM("storage")).value.toString
  }.map { s =>
    URI.create(s"$s$spotermeDirName/$gardensDirName/")
  }

  def create(g: AllotmentGarden): Future[AllotmentGarden] = {
    val gardenIri = IRI(g.uri)
    val gardenIriS = gardenIri.toString + "/"
    val sub = RDFLib.sym(gardenIriS)
    val doc = RDFLib.sym(gardenIriS + ".meta")
    val sts = List(
      RDFLib.st(sub, RDFHelper.RDF("type"), RDFHelper.PROD("Allotment_(gardening)"), doc),
      RDFLib.st(sub, RDFHelper.RDF("type"), RDFHelper.GOOD_REL("Individual"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("name"), RDFLib.literal(g.title, "de"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("description"), RDFLib.literal(g.description, "de"), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("image"), RDFLib.literal("images/"), doc),
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
    val baseIri = gardenIri.baseIRI
    val uuid = gardenIri.lastPathComponent
    for {
      _ <- RDFHelper.createContainerResource(baseIri.innerUri, uuid)
      _ <- RDFHelper.addStatementsToWeb(sts)
    } yield g
  }

  private def populateLoadedGarden(allotmentUri: URI)(imageUris: Seq[URI]): AllotmentGarden = {
    val imagesUrisOrPlaceholder = if (imageUris.nonEmpty) imageUris else List(
      new URI("/public/kgv/images/image-1.svg"),
      new URI("/public/kgv/images/image-2.svg"),
      new URI("/public/kgv/images/image-3.svg"))

    val allotmentTitle = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("name"))
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

    AllotmentGarden(
      uri = allotmentUri,
      title = allotmentTitle.toString,
      images = imagesUrisOrPlaceholder,
      description = allotmentDesc.toString,
      location = location,
      address = address,
      bungalow = if (!includes.toString.isEmpty) Some(Bungalow()) else None,
      area = Area(width.toString.toDouble * depth.toString.toDouble),
      condition = AllotmentCondition.namesToValuesMap.getOrElse(condition.toString, AllotmentCondition.Undefined)
    )
  }

}
