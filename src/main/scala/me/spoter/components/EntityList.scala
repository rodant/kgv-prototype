package me.spoter.components

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap.{Form, NavLink}
import me.spoter.models.KGVEntity

object EntityList {

  case class Props(entityUriFragment: String, es: Iterable[KGVEntity])

  private val component = ScalaComponent
    .builder[Props]("EntityList")
    .render_P(P => Form()(P.es.toTagMod(renderEntity(P.entityUriFragment))))
    .build

  def apply(entityUriFragment: String, es: Iterable[KGVEntity]): VdomElement = component(Props(entityUriFragment, es)).vdomElement

  private def renderEntity(uriFragment: String)(e: KGVEntity): VdomElement = {
    NavLink(href = s"#$uriFragment?uri=${e.uri}")(e.name.value)
  }
}
