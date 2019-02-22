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

      GardenService.fetchGarden(gardenUri).zip(fetchOfferor(offerorUri))
        .map[AllotmentOffering] { case (g, u) =>
        createOffering(offeringUri, g, u)
      }
    }.flatten

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

  private def fetchOfferor(offerorUri: URI): Future[User] = {
    RDFHelper.loadEntity(offerorUri) {
      val hasEmailNode = RDFHelper.get(offerorUri, RDFHelper.VCARD("hasEmail"))
      hasEmailNode match {
        case n if js.isUndefined(n) => Future(User(offerorUri))
        case _ =>
          val emailUri = new URI(hasEmailNode.value.toString)
          RDFHelper.loadEntity(emailUri)(
            User(offerorUri, Some(new URI(RDFHelper.get(emailUri, RDFHelper.VCARD("value")).value.toString)))
          )
      }
    }.flatten
  }
}
