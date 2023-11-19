package io.icednut.karat.scala.exercise

import cats.data.Kleisli
import cats.effect.IO
import io.icednut.karat.scala.exercise.model.ProductCatalog
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.http4s.client.Client
import org.http4s.implicits.uri
import org.http4s.{Method, Request, Response}
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.scalacheck.effect.PropF
import org.scalacheck.{Arbitrary, Gen}

class Http4sStateSpec extends CatsEffectSuite with ScalaCheckEffectSuite:

  given pcArb: Arbitrary[ProductCatalog] = Arbitrary {
    for {
      id <- Gen.uuid.map(_.toString)
      name <- Gen.identifier
      quantity <- Gen.choose[Long](0, 1000)
    } yield ProductCatalog(id, name, quantity)
  }

  test("checkRight") {
    PropF.forAllF { (p: ProductCatalog) =>
      val storage: ProductStorage[IO] = ProductStorage.impl[IO].unsafeRunSync()
      val httpApp: Kleisli[IO, Request[IO], Response[IO]] = ProductCatalogRoutes.routes[IO](storage).orNotFound
      val client: Client[IO] = Client.fromHttpApp(httpApp)

      val postRequest: Request[IO] = Request(method = Method.POST, uri = uri"/products").withEntity(p)
      val getRequest: Request[IO] = Request(method = Method.GET, uri = uri"/products")

      val statusResponse = for {
        status <- client.status(postRequest)
        response <- client.expect[List[ProductCatalog]](getRequest)
      } yield {
        status.code -> response
      }

      assertIO(statusResponse, (202, List(p)))
    }
  }
