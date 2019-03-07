package me.spoter.pages

import java.net.URI
import java.util.UUID

import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.{Callback, ScalaComponent}
import me.spoter.models.{AllotmentGarden, IRI}
import me.spoter.services.GardenService
import me.spoter.solid_libs.{RDFHelper, RDFLib}
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
        val garden = sxs.state.addingEntity.get.asInstanceOf[AllotmentGarden].copy(uri = gardenUri)
        performRDFOps(garden)
      }
    val updatedStateF = createdGardenF.map { garden =>
      bs.modState(old =>
        old.copy(state = old.state.copy(es = garden :: old.state.es.toList, addingEntity = None)))
    }
    updatedStateF
  }

  private def performRDFOps(g: AllotmentGarden): Future[AllotmentGarden] = {
    val gardenIri = IRI(g.uri)
    val gardenIriS = gardenIri.toString + "/"
    val sub = RDFLib.sym(gardenIriS)
    val doc = RDFLib.sym(gardenIriS + ".meta")
    val sts = List(
      RDFLib.st(sub, RDFHelper.RDF("type"), RDFHelper.PROD("Allotment_(gardening)"), doc),
      RDFLib.st(sub, RDFHelper.RDF("type"), RDFHelper.GOOD_REL("Individual"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("name"), RDFLib.literal(g.title, "de"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("description"), RDFLib.literal(g.description, "de"), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("image"), RDFLib.literal("images/"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("width"), RDFLib.literal("1"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("depth"), RDFLib.literal(g.area.a.toString), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("streetAddress"), RDFLib.literal(g.address.streetAndNumber, "de"), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("postalCode"), RDFLib.literal(g.address.zipCode.toString), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("addressRegion"), RDFLib.literal(g.address.region, "de"), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("addressCountry"), RDFLib.literal(g.address.country, "de"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("includes"), RDFLib.literal(g.bungalow.fold("")(_ => "Bungalow"), "de"), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("latitude"), RDFLib.literal(g.location.latitude.toString, typ = RDFHelper.XMLS("float")), doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("longitude"), RDFLib.literal(g.location.longitude.toString, typ = RDFHelper.XMLS("float")), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("condition"), RDFLib.literal(g.condition.toString, "de"), doc)
    )
    val baseIri = gardenIri.baseIRI
    val uuid = gardenIri.lastPathComponent
    for {
      _ <- RDFHelper.createContainerResource(baseIri.innerUri, uuid)
      _ <- RDFHelper.addStatementsToWeb(sts)
    } yield g
  }
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
