package io.hydrosphere.serving.manager.service.modelbuild

import java.util.Collections

import com.amazonaws.services.ecr.{AmazonECR, AmazonECRClientBuilder}
import com.amazonaws.services.ecr.model._
import com.spotify.docker.client.DockerClient
import io.hydrosphere.serving.manager.ECSDockerRepositoryConfiguration
import io.hydrosphere.serving.manager.model.ModelBuild
import io.hydrosphere.serving.model.ModelRuntime

/**
  *
  */
class ECSModelPushService(
  dockerClient: DockerClient,
  ecsDockerRepositoryConfiguration: ECSDockerRepositoryConfiguration
) extends ModelPushService {

  val ecrClient: AmazonECR = AmazonECRClientBuilder.standard()
    .withRegion(ecsDockerRepositoryConfiguration.region)
    .build()

  private def getDockerRegistryAuth: DockerRegistryAuth = {
    val getAuthorizationTokenRequest = new GetAuthorizationTokenRequest
    getAuthorizationTokenRequest.setRegistryIds(Collections.singletonList(ecsDockerRepositoryConfiguration.accountId))
    val result = ecrClient.getAuthorizationToken(getAuthorizationTokenRequest)

    val authorizationData = result.getAuthorizationData.get(0)

    DockerRegistryAuth(
      username = None,
      password = None,
      email = None,
      identityToken = None,
      serverAddress = Some(authorizationData.getProxyEndpoint),
      auth = Some(authorizationData.getAuthorizationToken)
    )
  }

  private def createRepositoryIfNeeded(modelName: String): Unit = {
    val req = new DescribeRepositoriesRequest
    req.setRepositoryNames(Collections.singletonList(modelName))
    req.setRegistryId(ecsDockerRepositoryConfiguration.accountId)
    try {
      ecrClient.describeRepositories(req)
    } catch {
      case ex: RepositoryNotFoundException =>
        val createRepositoryRequest = new CreateRepositoryRequest
        createRepositoryRequest.setRepositoryName(modelName)
        ecrClient.createRepository(createRepositoryRequest)
    }
  }


  override def getImageName(modelBuild: ModelBuild): String = {
    s"${ecsDockerRepositoryConfiguration.accountId}.dkr.ecr.${ecsDockerRepositoryConfiguration.region.getName}.amazonaws.com/${modelBuild.model.name}"
  }

  override def push(modelRuntime: ModelRuntime, progressHandler: ProgressHandler): Unit = {
    createRepositoryIfNeeded(modelRuntime.modelName)

    dockerClient.push(
      s"${modelRuntime.imageName}:${modelRuntime.modelVersion}",
      DockerClientHelper.createProgressHadlerWrapper(progressHandler),
      DockerClientHelper.createRegistryAuth(getDockerRegistryAuth)
    )
  }
}
