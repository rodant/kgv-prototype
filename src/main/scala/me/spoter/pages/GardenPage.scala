package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{CallbackTo, ScalaComponent}
import me.spoter.components.bootstrap._
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
          Form(
            Row(
              Col() {
                Carousel(
                  CarouselItem(
                    <.img(^.src := "assets/images/image-1.svg", ^.alt := "Bild 1", ^.className := "d-block w-100")
                  ),
                  CarouselItem(
                    <.img(^.src := "assets/images/image-2.svg", ^.alt := "Bild 2", ^.className := "d-block w-100")
                  ),
                  CarouselItem(
                    <.img(^.src := "assets/images/image-3.svg", ^.alt := "Bild 3", ^.className := "d-block w-100")
                  )
                )
              },
              Col() {
                FormGroup(controlId = "location") {
                  Row(
                    FormLabel(column = true)("Standort:"),
                    Col() {
                      FormControl(defaultValue = garden.location.toString, readOnly = true, plaintext = true)
                    }
                  )
                }
              },
              Col()(
                FormGroup(controlId = "size") {
                  Row(
                    FormLabel(column = true)("Größe:"),
                    Col() {
                      FormControl(defaultValue = garden.area.a.toString, readOnly = true, plaintext = true)
                    }
                  )
                },
                FormGroup(controlId = "address") {
                  Row(
                    FormLabel(column = true)("Adresse:"),
                    Col() {
                      FormControl(defaultValue = garden.address.toString, readOnly = true, plaintext = true)
                    }
                  )
                },
                FormGroup(controlId = "price") {
                  Row(
                    FormLabel(column = true)("Preis:"),
                    Col() {
                      FormControl(defaultValue = (garden.price.a / 100).toString, readOnly = true, plaintext = true)
                    }
                  )
                }
              )
            ),
            Row(
              Col(xl = "8", lg = "8", md = "8") {
                FormGroup(controlId = "description") {
                  FormControl(as = "textarea", defaultValue = garden.description, rows = 10, plaintext = true)
                }
              },
              Col()(
                FormGroup(controlId = "bungalow") {
                  Row(
                    FormLabel(column = true)("Bungalow:"),
                    Col() {
                      FormControl(defaultValue = garden.bungalow.map(_ => "Ja").getOrElse("Nein"), readOnly = true, plaintext = true)
                    }
                  )
                },
                FormGroup(controlId = "condition") {
                  Row(FormLabel(column = true)("Zustand:"),
                    Col() {
                      FormControl(defaultValue = garden.gardenCondition.toString, readOnly = true, plaintext = true)
                    }
                  )
                }
              ),
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
      uri = uri,
      title = "Mein Kleingarten",
      address = Address("Slaa Straße", "14A", 12345, "Berlin", "Deutschland"),
      location = Location(52.563464, 13.420226),
      area = Area(500),
      price = Money(300000),
      description = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
    )
  }
}
