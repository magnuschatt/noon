package nebula.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory

private val config: DefaultDockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost("tcp://host:2375")
        .build()

private val dockerCmdExecFactory: JerseyDockerCmdExecFactory = JerseyDockerCmdExecFactory()
        .withReadTimeout(1000)
        .withConnectTimeout(1000)
        .withMaxTotalConnections(100)
        .withMaxPerRouteConnections(10)

val dockerClient: DockerClient = DockerClientBuilder.getInstance(config)
        .withDockerCmdExecFactory(dockerCmdExecFactory)
        .build()