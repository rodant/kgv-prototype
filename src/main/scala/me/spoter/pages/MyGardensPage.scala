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
    val gardenIriS = gardenIri.toString
    val st = RDFLib.st(
      subject = RDFLib.sym(gardenIriS),
      predicate = RDFHelper.RDF("type"),
      obj = RDFHelper.PROD("Allotment_(gardening)"),
      doc = RDFLib.sym(gardenIriS + "/.meta").doc())
    val baseIri = gardenIri.baseIRI
    val uuid = gardenIri.lastPathComponent
    for {
      _ <- RDFHelper.createContainerResource(baseIri.innerUri, uuid)
      _ <- RDFHelper.addStatementToWeb(st)
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
