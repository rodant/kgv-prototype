package me.spoter.routes

import japgolly.scalajs.react.extra.router.{RouterConfigDsl, StaticDsl}
import japgolly.scalajs.react.vdom.VdomElement
import me.spoter.components.items.{Item1Data, Item2Data, ItemsInfo}
import me.spoter.pages.ItemsPage

sealed abstract class Item(val title: String,
                           val routerPath: String,
                           val render: () => VdomElement)

object Item {

  case object Info extends Item("Info", "info", () => ItemsInfo())

  case object Item1 extends Item("Item1", "item1", () => Item1Data())

  case object Item2 extends Item("Item2", "item2", () => Item2Data())

  val menu: Vector[Item] = Vector(Info, Item1, Item2)

  val routes: StaticDsl.Rule[Item] = RouterConfigDsl[Item].buildRule { dsl =>
    import dsl._
    menu
      .map { i =>
        staticRoute(i.routerPath, i) ~> renderR(
          r => ItemsPage(ItemsPage.Props(i, r)))
      }
      .reduce(_ | _)
  }
}
