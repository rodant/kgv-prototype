package me.spoter.components

import japgolly.scalajs.react.component.builder.Lifecycle
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ReactEventFromInput, ScalaComponent}
import me.spoter.components.bootstrap._
import me.spoter.models.{IRI, KGVEntity}

object EntityList {

  case class Props(entityUriFragment: String, es: Iterable[KGVEntity], deleteHandler: Option[IRI => Callback])

  case class State(entityToDelete: Option[KGVEntity] = None)

  private val component = ScalaComponent
    .builder[Props]("EntityList")
    .initialState(State())
    .renderP(($, P) => Form()(
      <.div(
        P.es.toTagMod(renderEntity($)),
        renderConfirmDeletion($))))
    .build

  def apply(entityUriFragment: String, es: Iterable[KGVEntity], deleteHandler: Option[IRI => Callback] = None): VdomElement =
    component(Props(entityUriFragment, es, deleteHandler)).vdomElement

  private def renderEntity($: Lifecycle.RenderScope[Props, State, Unit])(e: KGVEntity): VdomElement = {
    val uriFragment = $.props.entityUriFragment
    <.div(
      Row()(
        Col(xl = 3, lg = 3, md = 3, sm = 9, xs = 9)(
          NavLink(href = s"#$uriFragment?uri=${e.uri}")(e.name.value)
        ),
        Col(xl = 9, lg = 9, md = 9, sm = 3, xs = 3)(
          <.i(^.className := "fas fa-times",
            ^.title := "Löschen",
            ^.color := "red",
            ^.marginLeft := 10.px,
            ^.verticalAlign := "bottom",
            ^.onClick --> $.modState(_.copy(entityToDelete = Some(e))))
        ).when($.props.deleteHandler.isDefined)
      )
    )
  }

  private def renderConfirmDeletion($: Lifecycle.RenderScope[Props, State, Unit]): VdomElement = {
    val close = (_: Unit) => $.modState(_.copy(entityToDelete = None))

    def confirmDeletion(e: ReactEventFromInput): Callback = {
      val deleteHandler = $.props.deleteHandler.get
      val uri = $.state.entityToDelete.get.uri
      deleteHandler(IRI(uri)).flatMap(close)
    }

    Modal(size = "sm", show = $.state.entityToDelete.isDefined, onHide = close)(
      ModalHeader(closeButton = true)(
        ModalTitle()("Garten Entfernen")
      ),
      ModalBody()("Wollen Sie wirklich den Garten löschen?"),
      ModalFooter()(
        Button(onClick = confirmDeletion(_))("Löschen")
      )
    )
  }
}
