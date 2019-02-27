package me.spoter.pages

import me.spoter.Session
import me.spoter.models.AllotmentGarden
import me.spoter.services.GardenService

import scala.concurrent.Future

object GardensPage extends EntityListPage[AllotmentGarden] {
  override protected val componentName: String = "GardenPage"

  override protected val entityRenderName: String = "Gärten"

  override protected val entityUriFragment: String = "gardens"

  override protected def fetchEntities(s: Session): Future[Iterable[AllotmentGarden]] = GardenService.fetchGardensBy(s)
}
