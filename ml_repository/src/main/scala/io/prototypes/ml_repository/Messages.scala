package io.prototypes.ml_repository

import java.nio.file.Path

import akka.actor.ActorRef
import io.prototypes.ml_repository.datasource.local.LocalSource
import io.prototypes.ml_repository.ml.Model
import io.prototypes.ml_repository.repository.IndexEntry
/**
  * Created by Bulat on 31.05.2017.
  */
object Messages {
  object RepositoryActor {
    case object GetIndex
    case class GetModelIndexEntry(name: String)
    case class GetModelFiles(name: String)
    case class GetFile(modelName: String, filePath: String)
  }

  object Watcher {
    case class Subscribe(actorRef: ActorRef)
    case object Tick
    case object ChangeDetected
  }

  object Indexer {
    case object Index
    case class IndexedModels(models: Seq[IndexEntry])
  }

  object FileFetcher {

    case class GetFile(index: IndexEntry, filePath: Path)

  }

}
