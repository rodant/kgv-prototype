package me.spoter.pages

import japgolly.scalajs.react.component.builder.Lifecycle.ComponentDidMount
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import me.spoter.components.EntityList
import me.spoter.components.bootstrap.Container
import me.spoter.models.AllotmentOffering
import me.spoter.services.{OfferingService, UserService}

import scala.concurrent.Future

object SearchResultPage {

  case class Props(district: String)

  private val component = ScalaComponent
    .builder[Props]("SearchResultPage")
    .initialState(Seq.empty[AllotmentOffering])
    .render_S { os =>
      Container(
        <.h1("Kleingartenangebote"),
        EntityList("offerings", os))
    }
    .componentDidMount(fetchOfferings)
    .build

  def apply(district: String): VdomElement = component(Props(district)).vdomElement

  private def fetchOfferings(c: ComponentDidMount[Props, Seq[AllotmentOffering], Unit]): Callback = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Callback.future {
      for {
        webIds <- UserService.findWebIds()
        offerings <- Future.sequence(webIds.map(OfferingService.fetchOfferingsByWebId)).map(_.flatten)
      } yield c.setState(offerings)
    }
  }
}
