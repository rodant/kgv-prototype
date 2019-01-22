package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{CallbackTo, ScalaComponent}
import me.spoter.components.bootstrap.{Col, Container, Row}
import me.spoter.models._
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

/**
  *
  */
object GardenPage {
  // This will choose between dev/prod depending on your scalac `-Xelide-below` setting
  val CssSettings: Exports with Settings = scalacss.devOrProdDefaults

  //val uriRegex =
  //  "(?:(https?|ircs?):\\/\\/(?:www\\.)?|www\\.)((?:(?:[-\\w]+\\.)+)[-\\w]+)(?::\\d+)?(?:\\/((?:[-a-zA-Z;./\\d#:_?=&,]*)))?"

  private val component = ScalaComponent
    .builder[Props]("GardenPage")
    .initialStateCallbackFromProps(fetchGarden)
    .render_PS {
      case (_, garden) =>
        Container(
          <.h1(garden.title),
          Row(
            Col(
              <.label(^.`for` := "location", "Standort:"),
              <.input(^.id := "location",
                ^.defaultValue := garden.location.toString,
                ^.readOnly := true)
            ),
            Col(
              <.label(^.`for` := "price", "Preis:"),
              <.input(^.id := "price",
                ^.defaultValue := garden.price.a / 100)
            )
          ),
          Row(
            Col(
              <.label(^.`for` := "bungalow", "Bungalow:"),
              <.input(^.id := "bungalow",
                ^.defaultValue := garden.bungalow
                  .map(_.uri)
                  .getOrElse("")
                  .toString,
                ^.readOnly := true)
            )
          )
        )
    }.build

  case class Props(gardenUri: String)

  def apply(props: Props): VdomElement = component(props).vdomElement

  def apply(uri: String): VdomElement = apply(Props(uri))

  def fetchGarden(props: Props): CallbackTo[Garden] = CallbackTo {
    val uri = new URI("http://www.user_x.spoter.me/gardens/#1")
    val districtURI = new URI("http://www.spoter.me/districts/#spandau")
    Garden(
      uri = districtURI,
      title = "Mein Kleingarten",
      address = Address("Slaa Straße", "14A", 12345, "Berlin", "Deutschland"),
      location = Location(52.563464, 13.420226),
      area = Area(500),
      price = Money(300000)
    )
  }
}
