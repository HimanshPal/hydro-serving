package io.hydrosphere.serving.manager.repository

import java.time.LocalDateTime

import io.hydrosphere.serving.manager.model.{ModelBuild, ModelRuntime}
import io.hydrosphere.serving.manager.model.ModelBuildStatus.ModelBuildStatus

import scala.concurrent.Future

/**
  *
  */
trait ModelBuildRepository extends BaseRepository[ModelBuild, Long] {
  def lastByModelId(id: Long, maximum: Int): Future[Seq[ModelBuild]]

  def listByModelId(id: Long): Future[Seq[ModelBuild]]

  def finishBuild(id: Long, status: ModelBuildStatus, statusText: String, finished: LocalDateTime,
    modelRuntime: Option[ModelRuntime]): Future[Int]
}
