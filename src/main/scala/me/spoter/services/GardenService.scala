package me.spoter.services

import java.net.URI

import me.spoter.models._
import me.spoter.rdf.RdfLiteral
import me.spoter.services.rdf_mapping.BasicField._
import me.spoter.services.rdf_mapping.RdfField
import me.spoter.solid_libs.{RDFHelper, RDFLib}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

/**
  * RDF implementation of the garden service.
  */
object GardenService {
  private val spoterMeDirName = "spoterme"
  private val gardensDirName = "allotment_gardens"
  private val imagesDirName = "images"

  def imagesIRIFor(iri: IRI): IRI = IRI(s"${iri.toString}$imagesDirName")

  def fetchGardensByWebId(webId: URI, forceLoad: Boolean = false): Future[Seq[AllotmentGarden]] = {
    for {
      gardensDirUri <- fetchGardensDirByWebId(webId)
      gardenUris <- RDFHelper.listDir(gardensDirUri, forceLoad).recover[Seq[URI]] {
        case e if e.getMessage.contains("Not Found") || e.getMessage.contains("404") => Seq()
        case e =>
          println(s"Got unexpected server error ${e.getMessage},\n when fetching the gardens dir for user $webId")
          Seq()
      }
      gardens <- Future.sequence(gardenUris.map(uri => GardenService.fetchGarden(uri)))
    } yield gardens
  }

  def fetchGarden(allotmentUri: URI, forceLoad: Boolean = false): Future[AllotmentGarden] = {
    val gardenDirUri = if (allotmentUri.getPath.endsWith("/")) allotmentUri else URI.create(s"$allotmentUri/")
    RDFHelper.loadEntity[Future[AllotmentGarden]](gardenDirUri, forceLoad) {
      val imageDir = RDFHelper.get(gardenDirUri, RDFHelper.SCHEMA_ORG("image"))
      val imageDirUri = URI.create(s"$gardenDirUri${imageDir.toString}").normalize()

      RDFHelper.listDir(imageDirUri, forceLoad)
        .recover { case _ => Seq() }
        .map[AllotmentGarden](populateLoadedGarden(gardenDirUri))
    }.flatten
  }

  private def populateLoadedGarden(allotmentUri: URI)(imageUris: Seq[URI]): AllotmentGarden = {
    bestChoiceFor(allotmentUri, Name) match {
      case Name.default => AllotmentGarden(uri = allotmentUri, name = RdfLiteral(s"Dieser Garten ist fehlerhaft, id: $allotmentUri"))

      case name =>
        val allotmentDesc = bestChoiceFor(allotmentUri, Description)
        val latitude = bestChoiceFor(allotmentUri, Latitude)
        val longitude = bestChoiceFor(allotmentUri, Longitude)
        val location = Location(latitude, longitude)

        val streetAddress = bestChoiceFor(allotmentUri, StreetAndNumber)
        val postalCode = bestChoiceFor(allotmentUri, PostalCode)
        val addressRegion = bestChoiceFor(allotmentUri, AddressRegion)
        val addressCountry = bestChoiceFor(allotmentUri, AddressCountry)
        val address = Address(streetAddress, postalCode, addressRegion, addressCountry)

        val includes = bestOptChoiceFor(allotmentUri, BungalowField)
        val condition = bestChoiceFor(allotmentUri, Condition)

        val width = bestChoiceFor(allotmentUri, Width)
        val depth = bestChoiceFor(allotmentUri, Depth)

        val garden = AllotmentGarden(
          uri = allotmentUri,
          name = name,
          description = allotmentDesc,
          location = location,
          address = address,
          bungalow = includes.map(_ => Bungalow()),
          area = Area(width.value.toDouble * depth.value.toDouble),
          condition = AllotmentCondition.withNameInsensitiveOption(condition.value)
            .fold[AllotmentCondition](AllotmentCondition.Undefined)(c => c)
        )
        if (imageUris.nonEmpty) garden.copy(images = imageUris) else garden
    }
  }

  private def bestChoiceFor(sub: URI, field: RdfField): RdfLiteral =
    bestOptChoiceFor(sub, field).getOrElse(field.default)

  private def bestOptChoiceFor(sub: URI, field: RdfField): Option[RdfLiteral] = {
    val sts = RDFHelper.statementsMatching(Some(sub), Some(field.predicate), None, None)
    sts.find(_.why.value.toString.endsWith("/.meta"))
      .orElse(sts.headOption)
      .map(st => RdfLiteral.fromJSRflLiteral(st.`object`))
  }

  def fetchGardensDirByWebId(webId: URI): Future[URI] = RDFHelper.loadEntity(webId) {
    RDFHelper.get(webId, RDFHelper.PIM("storage")).value.toString
  }.map { s =>
    URI.create(s"$s$spoterMeDirName/$gardensDirName/")
  }

  def create(g: AllotmentGarden): Future[AllotmentGarden] = {
    val sts = gardenToSentences(g)
    val gardenIri = IRI(g.uri)

    val spoterResourceS = s"${g.uri.getScheme}://${g.uri.getHost}/$spoterMeDirName"
    val spoterResourceIri = IRI(spoterResourceS)
    for {
      _ <- RDFHelper.ensureContainerExists(spoterResourceIri)
      gardensResourceIri = IRI(s"$spoterResourceS/$gardensDirName")
      _ <- RDFHelper.ensureContainerExists(gardensResourceIri)
      canonicalGardenIri = gardenIri.removeTailingSlash
      baseIri = canonicalGardenIri.baseIRI
      uuid = canonicalGardenIri.lastPathComponent
      _ <- RDFHelper.createContainerResource(baseIri.innerUri, uuid)
      imagesIri = imagesIRIFor(gardenIri)
      _ <- RDFHelper.ensureContainerExists(imagesIri)
      _ <- RDFHelper.addStatementsToWeb(sts)
    } yield g
  }

  def delete(iri: IRI): Future[Unit] = {
    val imagesDir = imagesIRIFor(iri)
    for {
      g <- fetchGarden(iri.innerUri)
      _ <- Future.sequence(g.images.filter(!AllotmentGarden.defaultImages.contains(_))
        .map(uri => RDFHelper.deleteResource(IRI(uri))))
      _ <- RDFHelper.deleteResource(imagesDir)
      _ <- RDFHelper.deleteResource(iri)
    } yield ()
  }

  private def gardenToSentences(g: AllotmentGarden): List[js.Dynamic] = {
    val (sub, doc) = subAndDocFor(IRI(g.uri))
    List(
      RDFLib.st(sub, RDFHelper.RDF("type"), RDFHelper.PROD("Allotment_(gardening)"), doc),
      RDFLib.st(sub, RDFHelper.RDF("type"), RDFHelper.GOOD_REL("Individual"), doc),
      Name.st(sub, g.name, doc),
      Description.st(sub, g.description, doc),
      RDFLib.st(sub, RDFHelper.SCHEMA_ORG("image"), RDFLib.literal(s"$imagesDirName/"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("width"), RDFLib.literal("1"), doc),
      RDFLib.st(sub, RDFHelper.GOOD_REL("depth"), RDFLib.literal(g.area.a.toString), doc),
      StreetAndNumber.st(sub, g.address.streetAndNumber, doc),
      PostalCode.st(sub, g.address.postalCode, doc),
      AddressRegion.st(sub, g.address.region, doc),
      AddressCountry.st(sub, g.address.country, doc),
      Latitude.st(sub, g.location.latitude, doc),
      Longitude.st(sub, g.location.longitude, doc),
      Condition.st(sub, Condition.literal(g.condition), doc)
    ) ++ g.bungalow.map(b => BungalowField.st(sub, BungalowField.literal(b), doc)).toList
  }

  private def subAndDocFor(iri: IRI): (js.Dynamic, js.Dynamic) = {
    val iriS = iri.toString
    val sub = RDFLib.sym(iriS)
    val doc = RDFLib.sym(iriS + ".meta")
    (sub, doc)
  }

  def update(sub: IRI, field: RdfField, previous: RdfLiteral, next: RdfLiteral): Future[Unit] = {
    val (subSym, docSym) = subAndDocFor(sub)
    RDFHelper.updateStatement(previous, field.st(subSym, next, docSym))
  }

  def patch(sub: IRI, field: RdfField, previous: Option[RdfLiteral], next: Option[RdfLiteral]): Future[Unit] = {
    val (subSym, docSym) = subAndDocFor(sub)
    previous.fold(Future.unit)(l => RDFHelper.delStatementFromWeb(field.st(subSym, l, docSym)))
      .flatMap(_ => next.fold(Future.unit)(l => RDFHelper.addStatementToWeb(field.st(subSym, l, docSym))))
  }
}
