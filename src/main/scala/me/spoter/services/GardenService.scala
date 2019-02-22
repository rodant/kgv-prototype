package me.spoter.services

import java.net.URI

import me.spoter.models._
import me.spoter.solid_libs.RDFHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * RDF implementation of the garden service.
  */
object GardenService {
  def fetchGarden(allotmentUri: URI): Future[AllotmentGarden] =
    RDFHelper.loadEntity[Future[AllotmentGarden]](allotmentUri) {
      val imageDir = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("image"))
      val imageDirUri = new URI(s"${allotmentUri.toString}/${imageDir.toString}")

      RDFHelper.listDir(imageDirUri).map[AllotmentGarden](createGarden(allotmentUri))
    }.flatten

  private def createGarden(allotmentUri: URI)(imageUris: Seq[URI]): AllotmentGarden = {
    val imagesUrisOrPlaceholder = if (imageUris.nonEmpty) imageUris else List(
      new URI("/public/kgv/images/image-1.svg"),
      new URI("/public/kgv/images/image-2.svg"),
      new URI("/public/kgv/images/image-3.svg"))

    val allotmentTitle = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("name"))
    val allotmentDesc = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("description"))

    val latitude = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("latitude"))
    val longitude = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("longitude"))
    val location = Location(latitude.toString.toDouble, longitude.toString.toDouble)

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
