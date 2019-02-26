package me.spoter.pages

import me.spoter.Session
import me.spoter.models.AllotmentOffering
import me.spoter.services.OfferingService

import scala.concurrent.Future

/**
  *
  */
object OfferingsPage extends ObjectListPage[AllotmentOffering] {
  override protected val objectsName: String = "Gartenangebote"

  override protected val objectsUriFragment: String = "offerings"

  override protected def fetchListObjects(s: Session): Future[Iterable[AllotmentOffering]] = OfferingService.fetchOfferingsBy(s)
}
