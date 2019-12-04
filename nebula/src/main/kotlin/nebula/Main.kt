package nebula

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory


fun main() {

    val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("tcp://localhost:2375")
            .build()

    val dockerCmdExecFactory = JerseyDockerCmdExecFactory()
            .withReadTimeout(1000)
            .withConnectTimeout(1000)
            .withMaxTotalConnections(100)
            .withMaxPerRouteConnections(10)

    val client = DockerClientBuilder.getInstance(config)
            .withDockerCmdExecFactory(dockerCmdExecFactory)
            .build()

    val exec = client.removeContainerCmd("kin")
            .withForce(true)
            .exec()
    println(exec)

    val exposedPort = ExposedPort.tcp(7777)
    val portBinding = PortBinding(Ports.Binding.bindPort(7777), exposedPort)
    val hostConfig = HostConfig.newHostConfig().withPortBindings(portBinding)

    val create = client.createContainerCmd("kin")
            .withName("kin")
            .withExposedPorts(exposedPort)
            .withHostConfig(hostConfig)
            .exec()

    println(create)

    val resp = client.startContainerCmd("kin").exec()
    println(resp)

}