package io.icednut.karat.scala.exercise

import io.circe.Codec
import io.circe.generic.semiauto.*

import scala.util.control.NoStackTrace

package object model {

  case class ProductCatalog(id: String, name: String, quantity: Long)

  object ProductCatalog:
    implicit val productCatalogCodec: Codec[ProductCatalog] = deriveCodec[ProductCatalog]

  case object ProductAlreadyExistError extends RuntimeException("Product already exists") with NoStackTrace
}
