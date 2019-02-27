package me.spoter.pages

import me.spoter.Session
import me.spoter.models.AllotmentOffering
import me.spoter.services.OfferingService

import scala.concurrent.Future

object OfferingsPage extends EntityListPage[AllotmentOffering] {
  override protected val componentName: String = "OfferingsPage"

  override protected val entityRenderName: String = "Gartenangebote"

  override protected val entityUriFragment: String = "offerings"

  override protected def fetchEntities(s: Session): Future[Iterable[AllotmentOffering]] = OfferingService.fetchOfferingsBy(s)
}
