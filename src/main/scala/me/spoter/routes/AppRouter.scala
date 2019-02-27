package me.spoter.routes

import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.{Footer, TopNav}
import me.spoter.models.Menu
import me.spoter.pages._

object AppRouter {

  sealed trait AppPage

  case object Home extends AppPage

  case class Items(p: Item) extends AppPage

  case object Offerings extends AppPage

  case class Offering(uri: String) extends AppPage

  case object Gardens extends AppPage

  case class Garden(uri: String) extends AppPage

  private val config = RouterConfigDsl[AppPage].buildConfig { dsl =>
    import dsl._

    (trimSlashes
      | staticRoute(root, Home) ~> render(HomePage())
      | dynamicRouteCT[Offering]("#offerings?uri=" ~ string(".+").caseClass[Offering]) ~> dynRender(gp => OfferingPage(gp.uri))
      | dynamicRouteCT[Garden]("#gardens?uri=" ~ string(".+").caseClass[Garden]) ~> dynRender(gp => GardenPage(gp.uri))
      | staticRoute("#gardens", Gardens) ~> render(GardensPage())
      | staticRoute("#offerings", Offerings) ~> render(OfferingsPage()))
      .notFound(redirectToPage(Home)(Redirect.Replace))
      .renderWith(layout)
  }

  private val mainMenu = Vector(Menu(
    "Gärten", Garden("https://orisha1.solid.community/spoterme/allotment_gardens/13dd0a8d-443d-4b22-b7d9-1998b76a458a")),
    Menu("Gartenangebote", Offering("https://orisha1.solid.community/spoterme/offers/17be10f3-802f-42be-bbd0-bb03be89c812")))

  private def layout(c: RouterCtl[AppPage], r: Resolution[AppPage]) =
    <.div(
      TopNav(),
      r.render(),
      Footer()
    )

  private val baseUrl = BaseUrl.fromWindowOrigin / ""

  val router = Router(baseUrl, config)
}
