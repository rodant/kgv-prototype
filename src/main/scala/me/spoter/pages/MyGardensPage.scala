package me.spoter.pages

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.VdomElement
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
}

object MyGardensPage extends SessionTracker[Unit, State, MyGardensBackend] {
  private val componentName: String = "MyGardensPage"

  private val component = ScalaComponent
    .builder[Unit](componentName)
    .initialState(StateXSession[State](State(Seq()), Some(initialSession)))
    .renderBackend[MyGardensBackend]
    .componentDidMount(trackSessionOn(s => fetchEntities(s)))
    .componentWillUnmountConst(trackSessionOff())
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(): VdomElement = component().vdomElement

  private def fetchEntities(s: Session): Future[State] = GardenService.fetchGardensByWebId(s.webId).map(State(_))
}
