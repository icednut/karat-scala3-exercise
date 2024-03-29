package io.icednut.karat.scala.exercise

import cats.effect.{Async, Ref}
import cats.syntax.all.*
import io.icednut.karat.scala.exercise.model.{ProductAlreadyExistError, ProductCatalog}

trait ProductStorage[F[_]]:
  def all: F[List[ProductCatalog]]

  def find(id: String): F[Option[ProductCatalog]]

  def add(p: ProductCatalog): F[Either[ProductAlreadyExistError.type, Unit]]

  def update(p: ProductCatalog): F[Option[Unit]]

  def delete(id: String): F[Option[Unit]]

object ProductStorage:
  def impl[F[_] : Async]: F[ProductStorage[F]] = Ref.of[F, Map[String, ProductCatalog]](Map.empty).map(apply[F])

  private def apply[F[_] : Async](ref: Ref[F, Map[String, ProductCatalog]]): ProductStorage[F] = new ProductStorage[F]:
    override def all: F[List[ProductCatalog]] = ref.get.map(_.values.toList)

    override def find(id: String): F[Option[ProductCatalog]] = ref.get.map(_.get(id))

    override def add(p: ProductCatalog): F[Either[ProductAlreadyExistError.type, Unit]] = ref.modify { map =>
      if (map.contains(p.id)) {
        (map, Left(ProductAlreadyExistError))
      } else {
        (map + (p.id -> p), Right((): Unit))
      }
    }

    override def update(p: ProductCatalog): F[Option[Unit]] = ref.modify { map =>
      if (map.contains(p.id)) {
        (map + (p.id -> p), Some((): Unit))
      } else {
        (map, None)
      }
    }

    override def delete(id: String): F[Option[Unit]] = ref.modify { map =>
      if (map.contains(id)) {
        (map - id, Some((): Unit))
      } else {
        (map, None)
      }
    }
