package me.spoter.pages

import me.spoter.Session
import me.spoter.models.AllotmentGarden
import me.spoter.services.GardenService

import scala.concurrent.Future

/**
  *
  */
object GardensPage extends ObjectListPage[AllotmentGarden] {
  override protected val objectsName: String = "Gärten"

  override protected val objectsUriFragment: String = "gardens"

  override protected def fetchListObjects(s: Session): Future[Iterable[AllotmentGarden]] = GardenService.fetchGardensBy(s)
}
