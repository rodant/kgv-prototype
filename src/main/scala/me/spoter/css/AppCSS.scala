package me.spoter.css

import me.spoter.components.LeftNav
import me.spoter.pages.ItemsPage
import scalacss.internal.mutable.GlobalRegistry

object AppCSS {
  // This will choose between dev/prod depending on your scalac `-Xelide-below` setting
  private val CssSettings = scalacss.devOrProdDefaults

  import CssSettings._

  def load(): Unit = {
    GlobalRegistry.register(GlobalStyle,
      LeftNav.Style,
      ItemsPage.Style)
    GlobalRegistry.onRegistration(_.addToDocument())
  }
}
