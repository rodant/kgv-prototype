package me.spoter.components

import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent, _}
import me.spoter.components.bootstrap._
import me.spoter.models.IRI
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.raw.FormData

/**
  *
  */
object ImageCarousel {

  trait CommandHandler {
    def addImage(name: String, data: InputData): Callback

    def removeImage(iri: IRI): Callback
  }

  object NopCommandHandler$ extends CommandHandler {
    override def addImage(name: String, data: InputData): Callback = Callback()

    override def removeImage(iri: IRI): Callback = Callback()
  }

  case class Props(images: Seq[IRI], changeHandler: CommandHandler)

  case class State(images: Seq[IRI], adding: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) {
    def render(props: Props, state: State): VdomElement = {
      val editable = props.changeHandler != NopCommandHandler$
      <.div(
        Carousel(
          state.images.map { uri =>
            CarouselItem(
              <.img(^.src := uri.toString,
                ^.alt := "Bild x",
                ^.className := "d-block w-100")
            )
          }: _*
        ),
        <.div(^.marginTop := 10.px,
          <.i(^.className := "fas fa-plus",
            ^.title := "Bild hinzufügen",
            ^.color := "darkseagreen",
            ^.marginLeft := 10.px,
            ^.onClick --> bs.modState(_.copy(adding = true))),
          <.i(^.className := "fas fa-minus",
            ^.title := "Bild Entfernen",
            ^.color := "red",
            ^.marginLeft := 10.px,
            ^.onClick --> Callback.empty)
        ).when(editable),
        FormControl(`type` = "file", onChange = onFilesChange(_)).when(editable && state.adding)
      )
    }

    private def onFilesChange(e: ReactEventFromInput): Callback = {
      e.persist()
      e.stopPropagation()
      e.preventDefault()
      val file = e.target.files(0)
      val formData = new FormData()
      formData.append("file", file)
      val data = InputData.formdata2ajax(formData)
      for {
        handler <- bs.props.map(_.changeHandler)
        _ <- handler.addImage(file.name, data)
        _ <- Callback.alert(s"Sent file: $file")
        _ <- bs.modState(_.copy(adding = false))
      } yield ()
    }
  }

  private val component = ScalaComponent
    .builder[Props]("ImageCarousel")
    .initialStateFromProps(props => State(images = props.images))
    .renderBackend[Backend]
    .componentWillReceiveProps(c => c.modState(old => old.copy(images = c.nextProps.images)))
    .build

  def apply(images: Seq[IRI], commandHandler: CommandHandler = NopCommandHandler$): VdomElement = component(Props(images, commandHandler)).vdomElement

}
