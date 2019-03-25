package me.spoter

import japgolly.scalajs.react.Callback
import org.scalajs.dom.ext.KeyCode

/**
  *
  */
package object components {
  def handleEsc(escHandler: () => Callback): PartialFunction[Int, Callback] = {
    case KeyCode.Escape => escHandler()
  }

  def handleEnter(enterHandler: () => Callback): PartialFunction[Int, Callback] = {
    case KeyCode.Enter => enterHandler()
  }

  val ignoreKey: PartialFunction[Int, Callback] = {
    case _ => Callback()
  }

}