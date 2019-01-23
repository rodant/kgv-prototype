package me.spoter.components.bootstrap

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.{Children, JsComponent}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  *
  */

object Carousel {

  @JSImport("react-bootstrap", "Carousel")
  @js.native
  object RawComponent extends js.Object

  val component = JsComponent[Null, Children.Varargs, Null](RawComponent)

  def apply(children: ChildArg*): VdomElement = component(children: _*).vdomElement
}

object CarouselItem {

  @JSImport("react-bootstrap", "Carousel.Item")
  @js.native
  object RawComponent extends js.Object

  val component = JsComponent[Null, Children.Varargs, Null](RawComponent)

  def apply(children: ChildArg*): VdomElement = component(children: _*).vdomElement
}

object CarouselCaption {

  @JSImport("react-bootstrap", "Carousel.Caption")
  @js.native
  object RawComponent extends js.Object

  val component = JsComponent[Null, Children.Varargs, Null](RawComponent)

  def apply(children: ChildArg*): VdomElement = component(children: _*).vdomElement
}
