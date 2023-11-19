package io.icednut.karat.scala.exercise

import cats.data.OptionT
import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import io.icednut.karat.scala.exercise.model.*

object ProductCatalogRoutes:

  def routes[F[_] : Concurrent](storage: ProductStorage[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    given pcListEE: EntityEncoder[F, List[ProductCatalog]] = jsonEncoderOf[F, List[ProductCatalog]]

    given pcEE: EntityEncoder[F, ProductCatalog] = jsonEncoderOf[F, ProductCatalog]

    given pcED: EntityDecoder[F, ProductCatalog] = jsonOf[F, ProductCatalog]

    HttpRoutes.of[F] {
      case GET -> Root / "products" =>
        storage.all.flatMap(Ok(_))

      case GET -> Root / "products" / id =>
        OptionT(storage.find(id)).foldF(NotFound())(Ok(_))

      case req@POST -> Root / "products" =>
        for {
          product <- req.as[ProductCatalog]
          result <- storage.add(product)
          response <- result.fold(_ => BadRequest(), _ => Accepted())
        } yield {
          response
        }

      case req@PUT -> Root / "products" =>
        for {
          product <- req.as[ProductCatalog]
          result <- storage.update(product)
          response <- result.fold(NotFound())(_ => Ok())
        } yield {
          response
        }

      case DELETE -> Root / "products" / id =>
        for {
          result <- storage.delete(id)
          response <- result.fold(NotFound())(_ => NoContent())
        } yield {
          response
        }
    }
  }
