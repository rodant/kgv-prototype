package me.spoter.components

import java.net.URI

import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import me.spoter.components.bootstrap.{Carousel, CarouselItem}

/**
  *
  */
object ImageCarousel {

  case class Props(images: Seq[URI], editable: Boolean)

  case class State()

  class Backend(bs: BackendScope[Props, State]) {
    def render(props: Props, state: State): VdomElement =
      <.div(
        Carousel(
          props.images.map { uri =>
            CarouselItem(
              <.img(^.src := uri.toString,
                ^.alt := "Bild x",
                ^.className := "d-block w-100")
            )
          }: _*
        ),
        <.div(^.marginTop := 10.px,
          <.i(^.className := "fas fa-plus",
            ^.title := "Bestätigen",
            ^.color := "darkseagreen",
            ^.marginLeft := 10.px,
            ^.onClick --> Callback.empty),
          <.i(^.className := "fas fa-minus",
            ^.title := "Abbrechen",
            ^.color := "red",
            ^.marginLeft := 10.px,
            ^.onClick --> Callback.empty)
        ).when(props.editable)
      )
  }

  private val component = ScalaComponent
    .builder[Props]("ImageCarousel")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(images: Seq[URI], editable: Boolean = false): VdomElement = component(Props(images, editable)).vdomElement

}
