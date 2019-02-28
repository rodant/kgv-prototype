package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.component.builder.Lifecycle.ComponentDidMount
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import me.spoter.components.EntityList
import me.spoter.components.bootstrap.Container
import me.spoter.models.AllotmentOffering
import me.spoter.services.OfferingService

import scala.concurrent.Future

object SearchResultPage {

  case class Props(district: String)

  private val component = ScalaComponent
    .builder[Props]("SearchResultPage")
    .initialState(Seq.empty[AllotmentOffering])
    .render_S { os =>
      Container(EntityList("offerings", os))
    }
    .componentDidMount(fetchOfferings)
    .build

  def apply(district: String): VdomElement = component(Props(district)).vdomElement

  private val userIds = Seq(
    URI.create("https://orisha1.solid.community/profile/card#me"),
    URI.create("https://orisha2.solid.community/profile/card#me")
  )

  private def fetchOfferings(c: ComponentDidMount[Props, Seq[AllotmentOffering], Unit]): Callback = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Callback.future {
      Future.sequence(userIds.map(uid => OfferingService.fetchOfferingsByWebId(uid)))
        .map(_.flatten)
        .map(c.setState)
    }
  }
}
