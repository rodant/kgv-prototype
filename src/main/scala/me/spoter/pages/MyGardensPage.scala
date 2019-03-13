package me.spoter.pages

import java.net.URI
import java.util.UUID

import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.{Callback, ScalaComponent}
import me.spoter.models.AllotmentGarden
import me.spoter.services.GardenService
import me.spoter.{Session, SessionTracker, StateXSession}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MyGardensBackend(bs: BackendScope[Unit, StateXSession[State]]) extends EntityListBackend(bs) {
  override protected val entityUriFragment: String = "gardens"
  override protected val entityRenderName: String = "Gärten"

  override def render(sxs: StateXSession[State]): VdomElement = super.render(sxs)

  override protected def newEntity(): AllotmentGarden = AllotmentGarden()

  override protected def createEntity(sxs: StateXSession[State]): Callback = Callback.future {
    val uuid = UUID.randomUUID()
    val createdGardenF = GardenService.fetchGardensDirByWebId(sxs.session.get.webId)
      .flatMap { baseUri =>
        val gardenUri = URI.create(baseUri.toString + uuid).normalize()
        //TODO: get ride of the cast bellow
        val garden = sxs.state.addingEntity.get.asInstanceOf[AllotmentGarden].copy(uri = gardenUri)
        GardenService.create(garden)
      }
    createdGardenF.flatMap { _ =>
      fetchEntities(sxs.session.get, forceLoad = true).map(s => bs.modState(_.copy(state = s)))
    }
  }

  private[pages] def fetchEntities(s: Session, forceLoad: Boolean = false): Future[State] =
    GardenService.fetchGardensByWebId(s.webId, forceLoad).map(State(_))
}

object MyGardensPage extends SessionTracker[Unit, State, MyGardensBackend] {
  private val componentName: String = "MyGardensPage"

  private val component = ScalaComponent
    .builder[Unit](componentName)
    .initialState(StateXSession[State](State(Seq()), Some(initialSession)))
    .renderBackend[MyGardensBackend]
    .componentDidMount(c => trackSessionOn(s => c.backend.fetchEntities(s))(c))
    .componentWillUnmountConst(trackSessionOff())
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(): VdomElement = component().vdomElement
}
