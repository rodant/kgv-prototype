package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import me.spoter.components.bootstrap._
import me.spoter.models._
import me.spoter.rdf.RDFHelper
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

import scala.scalajs.js

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
    .initialState(initialGarden())
    .renderBackend[Backend]
    .componentDidMount(_.backend.fetchGarden)
    .build

  case class Props(gardenUri: String)

  def apply(props: Props): VdomElement = component(props).vdomElement

  def apply(uri: String): VdomElement = apply(Props(uri))

  class Backend(bs: BackendScope[Props, Garden]) {
    def render(props: Props, garden: Garden): VdomElement = {
      Container(
        <.h1(garden.title),
        Form(
          Row(
            Col() {
              Carousel(
                garden.images.map { uri =>
                  CarouselItem(
                    <.img(^.src := uri.toString,
                      ^.alt := "Bild x",
                      ^.className := "d-block w-100")
                  )
                }: _*
              )
            },
            Col() {
              FormGroup(controlId = "location") {
                Row(
                  FormLabel(column = true)("Standort:"),
                  Col() {
                    FormControl(
                      value = garden.location.toString,
                      readOnly = true,
                      plaintext = true)
                  }
                )
              }
            },
            Col()(
              FormGroup(controlId = "size") {
                Row(
                  FormLabel(column = true)("Größe:"),
                  Col() {
                    FormControl(
                      value = garden.area.a.toString,
                      readOnly = true,
                      plaintext = true)
                  }
                )
              },
              FormGroup(controlId = "address") {
                Row(
                  FormLabel(column = true)("Adresse:"),
                  Col() {
                    FormControl(
                      value = garden.address.toString,
                      readOnly = true,
                      plaintext = true)
                  }
                )
              },
              FormGroup(controlId = "price") {
                Row(
                  FormLabel(column = true)("Preis:"),
                  Col() {
                    FormControl(
                      value = (garden.price.a / 100).toString,
                      readOnly = true,
                      plaintext = true)
                  }
                )
              }
            )
          ),
          Row(
            Col(xl = 8, lg = 8, md = 8) {
              FormGroup(controlId = "description") {
                FormControl(
                  as = "textarea",
                  value = garden.description,
                  rows = 10,
                  readOnly = true,
                  plaintext = true)
              }
            },
            Col()(
              FormGroup(controlId = "bungalow") {
                Row(
                  FormLabel(column = true)("Bungalow:"),
                  Col() {
                    FormControl(
                      value = garden.bungalow.map(_ => "Ja").getOrElse("Nein"),
                      readOnly = true,
                      plaintext = true)
                  }
                )
              },
              FormGroup(controlId = "condition") {
                Row(FormLabel(column = true)("Zustand:"), Col() {
                  FormControl(
                    value = garden.gardenCondition.toString,
                    readOnly = true,
                    plaintext = true)
                })
              }
            ),
          )
        )
      )
    }

    def fetchGarden: Callback = Callback {
      val allotmentUri = new URI("https://orisha1.solid.community/spoterme/allotment_gardens/13dd0a8d-443d-4b22-b7d9-1998b76a458a/")
      RDFHelper.load(allotmentUri)
        .then[Garden] { _ =>
        val allotmentTitle = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("name"))
        val allotmentDesc = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("description"))

        val uri = new URI("http://www.user_x.spoter.me/gardens/#1")
        val allotment = Garden(
          uri = uri,
          title = allotmentTitle.toString,
          description = allotmentDesc.toString
        )
        allotment
      }.then[Unit](g => {
        bs.modState(prev => g.copy(images = prev.images)).runNow()
        ()
      }, js.UndefOr.any2undefOrA(_ => ()))
    }
  }

  def initialGarden(): Garden = {
    val uri = new URI("http://www.user_x.spoter.me/gardens/#1")
    Garden(
      uri = uri,
      images = List(new URI("assets/images/image-1.svg"))
    )
  }
}
