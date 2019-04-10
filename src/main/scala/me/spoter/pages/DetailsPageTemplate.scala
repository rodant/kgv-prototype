package me.spoter.pages

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap._

/**
  * Template for the detail garden and offering pages.
  */
trait DetailsPageTemplate {
  def fillInLayout(nameSlot: VdomElement,
                   imageSlot: VdomElement,
                   mapSlot: VdomElement,
                   sizeSlot: VdomElement,
                   addressSlot: VdomElement,
                   priceSlot: Option[VdomElement] = None,
                   descriptionSlot: VdomElement,
                   bungalowSlot: VdomElement,
                   conditionSlot: VdomElement,
                   availableAfterSlot: Option[VdomElement] = None,
                   contactSlot: Option[VdomElement] = None): VdomElement =
    Container(
      Form(validated = true)(^.noValidate := true)(
        Row()(
          nameSlot
        ),
        Row()(
          Col(sm = 12, xs = 12)(
            imageSlot
          ),
          Col(sm = 12, xs = 12)(
            <.div(^.height := 280.px,
              mapSlot
            )
          ),
          Col(sm = 12, xs = 12)(
            FormGroup(controlId = "size") {
              Row()(
                Col(xl = 4, lg = 4, md = 4, sm = 3, xs = 3)(
                  FormLabel(column = true)("Größe:")
                ),
                Col(xl = 8, lg = 8, md = 8, sm = 9, xs = 9) {
                  sizeSlot
                }
              )
            },
            FormGroup(controlId = "address") {
              Row()(
                Col(xl = 4, lg = 4, md = 4, sm = 3, xs = 3)(
                  FormLabel(column = true)("Adresse:")
                ),
                Col(xl = 8, lg = 8, md = 8, sm = 9, xs = 9) {
                  addressSlot
                }
              )
            },
            priceSlot.fold(EmptyVdom) { content =>
              FormGroup(controlId = "price") {
                Row()(
                  Col(xl = 4, lg = 4, md = 4, sm = 3, xs = 3)(
                    FormLabel(column = true)("Preis:")
                  ),
                  Col(xl = 8, lg = 8, md = 8, sm = 9, xs = 9) {
                    content
                  }
                )
              }
            }
          )
        ),
        Row()(
          Col(xl = 8, lg = 8, md = 8, sm = 12, xs = 12) {
            FormGroup(controlId = "description")(
              descriptionSlot
            )
          },
          Col(sm = 12, xs = 12)(
            FormGroup(controlId = "bungalow") {
              Row()(
                Col(xl = 4, lg = 4, md = 4, sm = 6, xs = 6)(
                  FormLabel(column = true)("Bungalow:")
                ),
                Col(xl = 8, lg = 8, md = 8, sm = 6, xs = 6) {
                  bungalowSlot
                }
              )
            },
            FormGroup(controlId = "condition") {
              Row()(
                Col(xl = 4, lg = 4, md = 4, sm = 6, xs = 6)(
                  FormLabel(column = true)("Gartenzustand:")
                ),
                Col(xl = 8, lg = 8, md = 8, sm = 6, xs = 6) {
                  conditionSlot
                }
              )
            },
            availableAfterSlot.fold(EmptyVdom) { content =>
              FormGroup(controlId = "availabilityStarts") {
                Row()(
                  Col(xl = 4, lg = 4, md = 4, sm = 6, xs = 6)(
                    FormLabel(column = true)("Verfügbar ab:")
                  ),
                  Col(xl = 8, lg = 8, md = 8, sm = 6, xs = 6) {
                    content
                  }
                )
              }
            },
            contactSlot.fold(EmptyVdom) { content =>
              FormGroup(controlId = "contact") {
                Row()(
                  Col(xl = 4, lg = 4, md = 4, sm = 6, xs = 6)(
                    FormLabel(column = true)("Kontakt:")
                  ),
                  Col(xl = 8, lg = 8, md = 8, sm = 6, xs = 6) {
                    content
                  }
                )
              }
            }
          )
        )
      )
    )
}
