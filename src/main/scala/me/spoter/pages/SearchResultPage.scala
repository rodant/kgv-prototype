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

  private val offeringsUris = Seq(
    "https://orisha1.solid.community/spoterme/offers/17be10f3-802f-42be-bbd0-bb03be89c812",
    "https://orisha1.solid.community/spoterme/offers/630cedbb-162a-4021-b38c-38cb7b6ed5d7",
    "https://orisha2.solid.community/spoterme/offers/94e1194d-33a9-46de-b45b-100d17fd4236"
  )

  private def fetchOfferings(c: ComponentDidMount[Props, Seq[AllotmentOffering], Unit]): Callback = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Callback.future {
      Future.sequence(offeringsUris.map(uStr => OfferingService.fetchOffering(URI.create(uStr)))).map(c.setState)
    }
  }
}
