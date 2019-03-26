package me.spoter.components

import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent, _}
import me.spoter.components.bootstrap._
import me.spoter.models.IRI
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.raw.FileReader

import scala.concurrent.Promise
import scala.scalajs.js.typedarray.ArrayBufferView

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

  case class State(adding: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) {
    def render(props: Props, state: State): VdomElement = {
      val editable = props.changeHandler != NopCommandHandler$
      <.div(
        Carousel(
          props.images.map { uri =>
            CarouselItem(
              <.img(^.src := uri.toString,
                ^.maxHeight := 220.px,
                ^.alt := "Garten-Bild",
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
        FormControl(`type` = "file", onChange = onFilesChange(_))(^.autoFocus := true).when(editable && state.adding)
      )
    }

    private def onFilesChange(e: ReactEventFromInput): Callback = {
      import scala.concurrent.ExecutionContext.Implicits.global
      e.persist()
      e.stopPropagation()
      e.preventDefault()
      val file = e.target.files(0)
      val promise = Promise[ArrayBufferView]()
      val reader = new FileReader()
      reader.onloadend = e => {
        if (e.loaded == file.size) {
          promise.success(reader.result.asInstanceOf[ArrayBufferView])
        } else {
          promise.failure(new Exception(s"Error reading the file: ${file.name}"))
        }
      }
      reader.readAsArrayBuffer(file)
      Callback.future {
        promise.future.map { data =>
          for {
            handler <- bs.props.map(_.changeHandler)
            _ <- handler.addImage(file.name, InputData.arrayBufferView2ajax(data))
            _ <- bs.modState(_.copy(adding = false))
          } yield ()
        }
      }
    }
  }

  private val component = ScalaComponent
    .builder[Props]("ImageCarousel")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(images: Seq[IRI], commandHandler: CommandHandler = NopCommandHandler$): VdomElement = component(Props(images, commandHandler)).vdomElement

}
