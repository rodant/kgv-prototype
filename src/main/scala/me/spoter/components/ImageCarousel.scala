package me.spoter.components

import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent, _}
import me.spoter.components.bootstrap._
import me.spoter.models.IRI
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.raw.FileReader

import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.typedarray.ArrayBufferView

/**
  *
  */
object ImageCarousel {

  trait CommandHandler {
    def addImage(name: String, data: InputData): Callback

    def removeImage(index: Int): Callback
  }

  object NopCommandHandler extends CommandHandler {
    override def addImage(name: String, data: InputData): Callback = Callback()

    override def removeImage(index: Int): Callback = Callback()
  }

  case class Props(images: Seq[IRI], activeIndex: Int, commandHandler: CommandHandler)

  case class State(currentIndex: Int = 0, adding: Boolean = false, deleting: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) {
    private val onSelectHandler: Option[(js.Any, String, js.Object) => Callback] =
      Option((eventKey, _, _) => bs.modState(_.copy(currentIndex = eventKey.asInstanceOf[Int])))

    def render(props: Props, state: State): VdomElement = {
      import scala.scalajs.js.JSConverters._
      val editable = props.commandHandler != NopCommandHandler
      <.div(^.paddingBottom := 10.px,
        Carousel(interval = null, activeIndex = state.currentIndex, onSelect = onSelectHandler.orUndefined)(
          props.images.map { uri =>
            CarouselItem(
              <.img(^.src := uri.toString,
                ^.alt := "Garten-Bild",
                ^.className := "d-block w-100")
            )
          }: _*
        ),
        <.div(^.marginTop := 10.px,
          <.i(^.className := "fas fa-plus",
            ^.title := "Bild Hinzufügen",
            ^.color := "darkseagreen",
            ^.marginLeft := 10.px,
            ^.onClick --> bs.modState(_.copy(adding = true))),
          <.i(^.className := "fas fa-minus",
            ^.title := "Aktuelles Bild Entfernen",
            ^.color := "red",
            ^.marginLeft := 10.px,
            ^.onClick --> checkOnDelete(bs))
        ).when(editable),
        Row()(
          Col(xl = 10, lg = 10, md = 10, sm = 11, xs = 11)(
            FormControl(`type` = "file", onChange = onFilesChange(_))(^.autoFocus := true),
          ),
          Col(xl = 2, lg = 2, md = 2, sm = 1, xs = 1)(
            <.i(^.className := "fas fa-times",
              ^.title := "Abbrechen",
              ^.color := "red",
              ^.marginLeft := 10.px,
              ^.onClick --> bs.modState(_.copy(adding = false)))
          )
        ).when(editable && state.adding),

        renderConfirmDeletion(state, bs).when(state.deleting)
      )
    }

    private def renderConfirmDeletion(state: State, bs: BackendScope[Props, State]): VdomElement = {
      val close = (_: Unit) => bs.modState(_.copy(deleting = false))

      def confirmDeletion(e: ReactEventFromInput): Callback = deleteImage(bs).flatMap(close)

      Modal(size = "sm", show = state.deleting, onHide = close)(
        ModalHeader(closeButton = true)(
          ModalTitle()("Bild Entfernen")
        ),
        ModalBody()("Wollen Sie das aktuelle Bild wirklich löschen?"),
        ModalFooter()(
          Button(onClick = confirmDeletion(_))("Löschen")
        )
      )
    }

    private def checkOnDelete(bs: BackendScope[Props, State]): Callback = bs.modState(_.copy(deleting = true))

    private def deleteImage(bs: BackendScope[Props, State]): Callback = {
      for {
        state <- bs.state
        props <- bs.props
        handler = props.commandHandler
        r <- handler.removeImage(state.currentIndex)
      } yield r
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
            handler <- bs.props.map(_.commandHandler)
            _ <- handler.addImage(file.name, InputData.arrayBufferView2ajax(data))
            _ <- bs.modState(_.copy(adding = false))
          } yield ()
        }
      }
    }
  }

  private val component = ScalaComponent
    .builder[Props]("ImageCarousel")
    .initialStateFromProps(props => State(currentIndex = props.activeIndex))
    .renderBackend[Backend]
    .componentWillReceiveProps(c => c.modState(old => old.copy(currentIndex = c.nextProps.activeIndex)))
    .build

  def apply(images: Seq[IRI], activeIndex: Int = 0, commandHandler: CommandHandler = NopCommandHandler): VdomElement =
    component(Props(images, activeIndex, commandHandler)).vdomElement

}
