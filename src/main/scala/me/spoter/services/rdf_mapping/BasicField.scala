package me.spoter.services.rdf_mapping

import me.spoter.rdf.{LangAnnotation, RdfLiteral}
import me.spoter.solid_libs.RDFHelper

import scala.scalajs.js

/**
  * RDF basic fields.
  */
object BasicField {
  private val DEFAULT_LANG_ANNO = LangAnnotation("de")

  case object Name extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.GOOD_REL("name")
    override val default: RdfLiteral = RdfLiteral("", Some(DEFAULT_LANG_ANNO))
  }

  case object Description extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.GOOD_REL("description")
    override val default: RdfLiteral = RdfLiteral("", Some(DEFAULT_LANG_ANNO))
  }

  case object StreetAndNumber extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.SCHEMA_ORG("streetAddress")
    override val default: RdfLiteral = RdfLiteral("", Some(DEFAULT_LANG_ANNO))
  }

  case object PostalCode extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.SCHEMA_ORG("postalCode")
    override val default: RdfLiteral = RdfLiteral("0")
  }

  case object AddressRegion extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.SCHEMA_ORG("addressRegion")
    override val default: RdfLiteral = RdfLiteral("", Some(DEFAULT_LANG_ANNO))
  }

  case object AddressCountry extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.SCHEMA_ORG("addressCountry")
    override val default: RdfLiteral = RdfLiteral("Deutschland", Some(DEFAULT_LANG_ANNO))
  }

  case object Latitude extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.SCHEMA_ORG("latitude")
    override val default: RdfLiteral = RdfLiteral("0")
  }

  case object Longitude extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.SCHEMA_ORG("longitude")
    override val default: RdfLiteral = RdfLiteral("0")
  }

  case object Width extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.GOOD_REL("width")
    override val default: RdfLiteral = RdfLiteral("0")
  }

  case object Depth extends RdfField {
    override val predicate: js.Dynamic = RDFHelper.GOOD_REL("depth")
    override val default: RdfLiteral = RdfLiteral("0")
  }

}
