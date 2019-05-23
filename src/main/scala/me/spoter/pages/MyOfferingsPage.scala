package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.{Callback, Reusability, ScalaComponent}
import me.spoter.models.{AllotmentGarden, AllotmentOffering, User}
import me.spoter.services.OfferingService
import me.spoter.{Session, SessionTracker, StateXSession}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MyOfferingsBackend(bs: BackendScope[Unit, StateXSession[State]]) extends EntityListBackend(bs) {
  override protected val entityUriFragment: String = "offerings"
  override protected val entityRenderName: String = "Gartenangebote"

  override def render(sxs: StateXSession[State]): VdomElement = super.render(sxs)

  override protected def newEntity(): AllotmentOffering =
    AllotmentOffering(offeredBy = User(URI.create("_blank")), garden = AllotmentGarden())

  override protected def createEntity(sxs: StateXSession[State]): Callback = Callback()
}

object MyOfferingsPage extends SessionTracker[Unit, State, MyOfferingsBackend] {
  private val componentName: String = "MyOfferingsPage"

  private val component = ScalaComponent
    .builder[Unit](componentName)
    .initialState(StateXSession[State](State(Seq()), Some(initialSession)))
    .renderBackend[MyOfferingsBackend]
    .componentDidMount(trackSessionOn(s => fetchEntities(s)))
    .componentWillUnmountConst(trackSessionOff())
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(): VdomElement = component().vdomElement

  private def fetchEntities(s: Session): Future[State] = OfferingService.fetchOfferingsByWebId(s.webId).map(State(_))
}
