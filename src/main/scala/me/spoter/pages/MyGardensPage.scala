package me.spoter.pages

import me.spoter.Session
import me.spoter.models.AllotmentGarden
import me.spoter.services.GardenService

import scala.concurrent.Future

object MyGardensPage extends EntityListPage[AllotmentGarden] {
  override protected val componentName: String = "MyGardenPage"

  override protected val entityRenderName: String = "Gärten"

  override protected val entityUriFragment: String = "gardens"

  override protected def fetchEntities(s: Session): Future[Iterable[AllotmentGarden]] = GardenService.fetchGardensByWebId(s.webId)
}
