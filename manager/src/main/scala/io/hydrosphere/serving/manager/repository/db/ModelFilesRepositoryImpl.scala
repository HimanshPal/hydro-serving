package io.hydrosphere.serving.manager.repository.db

import io.hydrosphere.serving.manager
import io.hydrosphere.serving.manager.db.Tables
import io.hydrosphere.serving.manager.model.{Model, ModelFile}
import io.hydrosphere.serving.manager.repository.ModelFilesRepository

import scala.concurrent.{ExecutionContext, Future}

class ModelFilesRepositoryImpl(databaseService: DatabaseService)(implicit executionContext: ExecutionContext) extends ModelFilesRepository {

  import ModelFilesRepositoryImpl._
  import databaseService._
  import databaseService.driver.api._

  override def create(entity: ModelFile): Future[ModelFile] =
    db.run(
      Tables.ModelFiles returning Tables.ModelFiles += Tables.ModelFilesRow(
        entity.id,
        entity.path,
        entity.model.id,
        entity.hashSum,
        entity.createdAt,
        entity.updatedAt
      )
    ).map(s => mapFromDb(s, entity.model))

  override def get(id: Long): Future[Option[ModelFile]] = ???

  override def delete(id: Long): Future[Int] =
    db.run(
      Tables.ModelFiles
        .filter(_.fileId === id)
        .delete
    )

  override def all(): Future[Seq[ModelFile]] = ???

  override def modelFiles(modelId: Long): Future[List[ModelFile]] =
    db.run(
      Tables.ModelFiles
        .filter(_.modelId === modelId)
        .join(Tables.Model)
        .on({case (mfid, mid) => mid.modelId === mfid.modelId})
        .result
    ).map(mapFromDb)

  override def get(filePath: String): Future[Option[ModelFile]] =
    db.run(
      Tables.ModelFiles
        .filter(_.filePath === filePath)
        .join(Tables.Model)
        .on({case (mfid, mid) => mid.modelId === mfid.modelId})
        .result
        .headOption
    ).map(mapFromDb)

  override def update(modelFile: ModelFile): Future[Int] = {
    val query = for {
      modelFiles <- Tables.ModelFiles if modelFiles.fileId === modelFile.id
    } yield (
      modelFiles.modelId,
      modelFiles.filePath,
      modelFiles.createdAt,
      modelFiles.updatedAt,
      modelFiles.hashSum
    )

    db.run(query.update(
      modelFile.model.id,
      modelFile.path,
      modelFile.createdAt,
      modelFile.updatedAt,
      modelFile.hashSum
    ))
  }


  override def deleteModelFiles(modelId: Long): Future[Int] =
    db.run(
      Tables.ModelFiles
        .filter(_.modelId === modelId)
        .delete
    )
}

object ModelFilesRepositoryImpl {

  def mapFromDb(s: _root_.io.hydrosphere.serving.manager.db.Tables.ModelFilesRow, model: Model): ModelFile = {
    ModelFile(
      s.fileId,
      s.filePath,
      model,
      s.hashSum,
      s.createdAt,
      s.updatedAt
    )
  }

  def mapFromDb(x: (Tables.ModelFilesRow, Tables.ModelRow)): ModelFile = {
    ModelFile(
      x._1.fileId,
      x._1.filePath,
      ModelRepositoryImpl.mapFromDb(x._2, None),
      x._1.hashSum,
      x._1.createdAt,
      x._1.updatedAt
    )
  }

  def mapFromDb(m: Option[(Tables.ModelFilesRow, Tables.ModelRow)]): Option[ModelFile] = {
    m.map(mapFromDb)
  }

  def mapFromDb(m: Seq[(Tables.ModelFilesRow, Tables.ModelRow)]): List[ModelFile] = {
    m.map(mapFromDb).toList
  }

}