package nebula

import com.charleskorn.kaml.Yaml
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory
import io.netty.channel.nio.NioEventLoopGroup
import kin.api.Kin
import kotlinx.serialization.Serializable
import java.io.File
import java.util.concurrent.TimeUnit

@Serializable
data class GoalStateFile(val services: Map<String, ServiceGoalState>)

@Serializable
data class ServiceGoalState(val image: String,
                            val httpPort: Int,
                            val count: Int)

val eventLoopGroup = NioEventLoopGroup()
val goalFile = File("nebula/goal.yaml")
val goalStateFile = Yaml.default.parse(GoalStateFile.serializer(), goalFile.readText())

val config: DefaultDockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost("tcp://localhost:2375")
        .build()

val dockerCmdExecFactory: JerseyDockerCmdExecFactory = JerseyDockerCmdExecFactory()
        .withReadTimeout(1000)
        .withConnectTimeout(1000)
        .withMaxTotalConnections(100)
        .withMaxPerRouteConnections(10)

val client: DockerClient = DockerClientBuilder.getInstance(config)
        .withDockerCmdExecFactory(dockerCmdExecFactory)
        .build()

fun main() {
    if (!goalFile.exists()) {
        goalFile.parentFile.mkdirs()
        goalFile.createNewFile()
    }
    eventLoopGroup.scheduleAtFixedRate(::check, 2000L, 2000L, TimeUnit.MILLISECONDS)
}

fun check() {
    val goalStateFile = Yaml.default.parse(GoalStateFile.serializer(), goalFile.readText())

    val containers = client.listContainersCmd()
            .withShowAll(true)
            .exec()

    for ((name, service) in goalStateFile.services) {
        val count = service.count
        for (i in 0 until count) {
            val containerName = "/$name$i"
            val existingContainer = containers.any { it.names.any { name -> name == containerName } }
            if (existingContainer) {
                continue
            }

            val exposedPort = ExposedPort.tcp(service.httpPort)
            val portBinding = PortBinding(Ports.Binding.bindPort(0), exposedPort)
            val hostConfig = HostConfig.newHostConfig().withPortBindings(portBinding)

            val create = client.createContainerCmd(service.image)
                    .withName(containerName)
                    .withExposedPorts(exposedPort)
                    .withHostConfig(hostConfig)
                    .exec()

            val resp = client.startContainerCmd(create.id).exec()
            println(resp)
        }
    }

}



//    list.forEach {
//        it.names.forEach { println(it) }
//    }
//    println(list)
//
//    val exec = client.removeContainerCmd("kin")
//            .withForce(true)
//            .exec()
//    println(exec)
//
//    val exposedPort = ExposedPort.tcp(7777)
//    val portBinding = PortBinding(Ports.Binding.bindPort(7777), exposedPort)
//    val hostConfig = HostConfig.newHostConfig().withPortBindings(portBinding)
//
//    val create = client.createContainerCmd("kin")
//            .withName("kin")
//            .withExposedPorts(exposedPort)
//            .withHostConfig(hostConfig)
//            .exec()
//
//    println(create)
//
//    val resp = client.startContainerCmd("kin").exec()
//    println(resp)
