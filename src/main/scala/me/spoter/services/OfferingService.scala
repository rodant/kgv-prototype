package me.spoter.services

import java.net.URI

import me.spoter.models.{AllotmentGarden, AllotmentOffering, Money, User}
import me.spoter.solid_libs.RDFHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

/**
  * RDF implementation of the offering service.
  */
object OfferingService {

  def fetchOffering(offeringUri: URI): Future[AllotmentOffering] =
    RDFHelper.loadEntity(offeringUri) {
      val gardenUri = new URI(RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("includes")).value.toString)
      val offerorUri = new URI(RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("offeredBy")).value.toString)

      GardenService.fetchGarden(gardenUri).zip(UserService.fetchUser(offerorUri))
        .map[AllotmentOffering] { case (g, u) =>
        createOffering(offeringUri, g, u)
      }
    }.flatten

  def fetchOfferingsByWebId(webId: URI): Future[Seq[AllotmentOffering]] = {
    for {
      storageUriStr <- RDFHelper.loadEntity(webId)(RDFHelper.get(webId, RDFHelper.PIM("storage")).value.toString)
      offerUris <- RDFHelper.listDir(new URI(s"$storageUriStr/spoterme/offers/").normalize())
        .recover[Seq[URI]] {
        case e if e.getMessage.contains("Not Found") || e.getMessage.contains("404") => Seq()
        case e =>
          println(s"Got unexpected server error ${e.getMessage},\n when fetching the offers dir for user $webId")
          Seq()
      }
      offers <- Future.sequence(offerUris.map(OfferingService.fetchOffering))
    } yield offers
  }

  private def createOffering(offeringUri: URI, g: AllotmentGarden, offeror: User): AllotmentOffering = {
    val title = RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("name"))
    val desc = RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("description"))
    val price = RDFHelper.get(offeringUri, RDFHelper.SCHEMA_ORG("price"))
    val availabilityStarts = RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("availabilityStarts")).toString

    AllotmentOffering(
      offeringUri,
      title.toString,
      desc.toString,
      Money(price.toString.toLong),
      offeredBy = offeror,
      availabilityStarts = new js.Date(availabilityStarts),
      garden = g
    )
  }
}
