package me.spoter.services

import me.spoter.models.{Address, Location}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  *
  */
object GeoCodingService {
  def locationFrom(newAddress: Address) = Future(Location(52.574157, 13.407999))
}
