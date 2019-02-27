package me.spoter.routes

import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.{Footer, TopNav}
import me.spoter.pages._

object AppRouter {

  sealed trait AppPage

  case object Home extends AppPage

  case class Items(p: Item) extends AppPage

  case object Offerings extends AppPage

  case class Offering(uri: String) extends AppPage

  case class SearchResult(district: String) extends AppPage

  case object Gardens extends AppPage

  case class Garden(uri: String) extends AppPage

  private val config = RouterConfigDsl[AppPage].buildConfig { dsl =>
    import dsl._

    (trimSlashes
      | staticRoute(root, Home) ~> render(HomePage())
      | dynamicRouteCT[Offering]("#offerings?uri=" ~ string(".+").caseClass[Offering]) ~> dynRender(ap => OfferingPage(ap.uri))
      | dynamicRouteCT[Garden]("#gardens?uri=" ~ string(".+").caseClass[Garden]) ~> dynRender(ap => GardenPage(ap.uri))
      | dynamicRouteCT[SearchResult]("#offerings?district=" ~ string(".+").caseClass[SearchResult])
      ~> dynRender(ap => SearchResultPage(ap.district))
      | staticRoute("#gardens", Gardens) ~> render(MyGardensPage())
      | staticRoute("#offerings", Offerings) ~> render(MyOfferingsPage()))
      .notFound(redirectToPage(Home)(Redirect.Replace))
      .renderWith(layout)
  }

  private def layout(c: RouterCtl[AppPage], r: Resolution[AppPage]) =
    <.div(
      TopNav(),
      r.render(),
      Footer()
    )

  private val baseUrl = BaseUrl.fromWindowOrigin / ""

  val router = Router(baseUrl, config)
}
