package me.spoter.models

/**
  * A GPS location.
  */
case class Location(latitude: Double, longitude: Double)

/**
  * Area in square meters
  *
  * @param a value
  */
case class Area(a: Double = 0) extends AnyVal

/**
  * Amount of money in €.
  *
  * @param amount amount of cents
  */
case class Money(amount: Long) extends AnyVal

import enumeratum._
import me.spoter.rdf.RdfLiteral
import me.spoter.services.rdf_mapping.BasicField.{AddressCountry, AddressRegion, PostalCode, StreetAndNumber}

import scala.collection.immutable

/**
  *
  */
sealed trait AllotmentCondition extends EnumEntry

object AllotmentCondition extends Enum[AllotmentCondition] {

  override def values: immutable.IndexedSeq[AllotmentCondition] = findValues

  case object Excellent extends AllotmentCondition

  case object Good extends AllotmentCondition

  case object Poor extends AllotmentCondition

  case object Undefined extends AllotmentCondition

}

/**
  *
  * @param streetAndNumber street and address string
  * @param postalCode      the zip code
  * @param region          something like city or state
  * @param country         the country
  */
case class Address(streetAndNumber: RdfLiteral = StreetAndNumber.default,
                   postalCode: RdfLiteral = PostalCode.default,
                   region: RdfLiteral = AddressRegion.default,
                   country: RdfLiteral = AddressCountry.default)