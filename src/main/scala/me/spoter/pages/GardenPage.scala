package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, CallbackTo, ScalaComponent}
import me.spoter.components.bootstrap._
import me.spoter.models.AllotmentCondition._
import me.spoter.models._
import me.spoter.rdf.RDFHelper
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.Thenable

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
    .initialState(AllotmentOffering(offeredBy = new URI(""), garden = AllotmentGarden()))
    .renderBackend[Backend]
    .componentDidMount(c => c.backend.fetchOffering(c.props))
    .build

  case class Props(uri: URI)

  def apply(props: Props): VdomElement = component(props).vdomElement

  def apply(id: String): VdomElement = apply(Props(new URI(s"https://orisha1.solid.community/spoterme/offers/$id")))

  class Backend(bs: BackendScope[Props, AllotmentOffering]) {
    def render(offering: AllotmentOffering): VdomElement = {
      val garden = offering.garden
      Container(
        <.h1(offering.title),
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
                  Col(xl = 8, lg = 8, md = 8) {
                    FormControl(
                      value = garden.location.toString,
                      readOnly = true,
                      plaintext = true)()
                  }
                )
              }
            },
            Col()(
              FormGroup(controlId = "size") {
                Row(
                  FormLabel(column = true)("Größe:"),
                  Col(xl = 8, lg = 8, md = 8) {
                    FormControl(
                      value = s"${garden.area.a} qm",
                      readOnly = true,
                      plaintext = true)()
                  }
                )
              },
              FormGroup(controlId = "address") {
                Row(
                  FormLabel(column = true)("Adresse:"),
                  Col(xl = 8, lg = 8, md = 8) {
                    FormControl(
                      value = s"${garden.address.streetAndNumber}, ${garden.address.zipCode} ${garden.address.region}",
                      readOnly = true,
                      plaintext = true)()
                  }
                )
              },
              FormGroup(controlId = "price") {
                Row(
                  FormLabel(column = true)("Preis:"),
                  Col(xl = 8, lg = 8, md = 8) {
                    FormControl(
                      value = (offering.price.amount / 100.0).formatted("%.2f €"),
                      readOnly = true,
                      plaintext = true)()
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
                  value = offering.description,
                  rows = 20,
                  readOnly = true,
                  plaintext = true)()
              }
            },
            Col()(
              FormGroup(controlId = "bungalow") {
                Row(
                  FormLabel(column = true)("Bungalow:"),
                  Col(xl = 8, lg = 8, md = 8) {
                    FormControl(
                      value = garden.bungalow.map(_ => "Ja").getOrElse[String]("Nein"),
                      readOnly = true,
                      plaintext = true)()
                  }
                )
              },
              FormGroup(controlId = "condition") {
                Row(
                  FormLabel(column = true)("Zustand:"),
                  Col(xl = 8, lg = 8, md = 8) {
                    FormControl(
                      value = garden.condition match {
                        case Excellent => "Ausgezeichnet"
                        case Good => "Gut"
                        case Poor => "Dürftig"
                        case Undefined => "KA"
                      },
                      readOnly = true,
                      plaintext = true)()
                  })
              },
              FormGroup(controlId = "availabilityStarts") {
                Row(
                  FormLabel(column = true)("Verfügbar ab:"),
                  Col(xl = 8, lg = 8, md = 8) {
                    FormControl(
                      value = offering.availabilityStarts.toLocaleDateString(),
                      readOnly = true,
                      plaintext = true)()
                  })
              },
              FormGroup(controlId = "contact") {
                Row(
                  FormLabel(column = true)("Kontakt:"),
                  Col(xl = 8, lg = 8, md = 8) {
                    FormControl(
                      value = offering.offeredBy.toString,
                      readOnly = true,
                      plaintext = true)()
                  })
              }
            ),
          )
        )
      )
    }

    def fetchOffering(props: Props): Callback = Callback {
      val offeringUri = props.uri
      RDFHelper.load(offeringUri)
        .then[Unit] { _ =>
        val title = RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("name"))
        val desc = RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("description"))
        val price = RDFHelper.get(offeringUri, RDFHelper.SCHEMA_ORG("price"))
        val availabilityStarts = RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("availabilityStarts")).toString
        val offerorUri = RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("offeredBy")).value

        val allotmentUri = RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("includes")).value
        val callbackOffering: CallbackTo[Thenable[AllotmentOffering]] = fetchGarden(new URI(allotmentUri.toString))
          .map { ta =>
            ta.then[AllotmentOffering](a =>
              AllotmentOffering(
                offeringUri,
                title.toString,
                desc.toString,
                Money(price.toString.toLong),
                offeredBy = new URI(offerorUri.toString),
                availabilityStarts = new js.Date(availabilityStarts),
                garden = a
              ), js.undefined)
          }

        val res: CallbackTo[Unit] = callbackOffering.map[Unit] { to: Thenable[AllotmentOffering] =>
          to.then[Unit](o => {
            bs.modState(_ => o).runNow()
            ()
          }, js.undefined)
        }
        res
          .runNow()
      }
    }

    private def fetchGarden(allotmentUri: URI) = CallbackTo[Thenable[AllotmentGarden]] {
      RDFHelper.load(allotmentUri)
        .then[AllotmentGarden] { _ =>
        val allotmentTitle = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("name"))
        val allotmentDesc = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("description"))

        val latitude = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("latitude"))
        val longitude = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("longitude"))
        val location = Location(latitude.toString.toDouble, longitude.toString.toDouble)

        val streetAddress = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("streetAddress"))
        val postalCode = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("postalCode"))
        val addressRegion = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("addressRegion"))
        val addressCountry = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("addressCountry"))
        val address = Address(streetAddress.toString, postalCode.toString.toInt, addressRegion.toString, addressCountry.toString)

        val includes = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("includes"))
        val condition = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("condition"))

        val width = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("width"))
        val depth = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("depth"))

        val imageDir = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("image"))
        val imageDirUri = new URI(allotmentUri.toString + imageDir.toString)
        RDFHelper.load(imageDirUri)
          .then[List[URI]] { _ =>
          val filesNodes: mutable.Seq[js.Dynamic] =
            RDFHelper.getAll(imageDirUri, RDFHelper.LDP("contains")).asInstanceOf[js.Array[js.Dynamic]]
          val files = filesNodes.map((f: js.Dynamic) => new URI(f.value.toString))
          files.toList
        }
          .then[AllotmentGarden](
          (imageUris: List[URI])
          => {
            val uri = new URI("http://www.user_x.spoter.me/gardens/#1")
            val allotment = AllotmentGarden(
              uri = imageDirUri,
              title = allotmentTitle.toString,
              images = if (imageUris.nonEmpty) imageUris else List(
                new URI("/public/kgv/images/image-1.svg"),
                new URI("/public/kgv/images/image-2.svg"),
                new URI("/public/kgv/images/image-3.svg")),
              description = allotmentDesc.toString,
              location = location,
              address = address,
              bungalow = if (!includes.toString.isEmpty) Some(Bungalow()) else None,
              area = Area(width.toString.toDouble * depth.toString.toDouble),
              condition = AllotmentCondition.namesToValuesMap.getOrElse(condition.toString, AllotmentCondition.Undefined)
            )
            allotment
          }, js.UndefOr.any2undefOrA(_ => AllotmentGarden()))
      }
    }
  }

}
