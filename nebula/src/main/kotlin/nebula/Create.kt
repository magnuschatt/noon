package nebula

import com.github.dockerjava.api.command.CreateContainerCmd
import com.github.dockerjava.api.model.*
import nebula.docker.dockerClient
import java.net.DatagramSocket
import java.net.InetAddress

private val localIp: String by lazy {
    DatagramSocket().use { socket ->
        socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
        socket.localAddress.hostAddress
    }
}

fun main() {
    createServiceInstance(0, "setty")
}

fun createServiceInstance(index: Int, image: String) {
    val networkId = createTunnelNetwork(index, image)
    createTunnel(index, image, networkId)
    createService(index, image, networkId)
}

fun createTunnel(index: Int, image: String, networkId: String) {
    val tunnelName = getTunnelContainerName(index, image)
    val tunnelImage = "tunnel"
    val alias = "tunnel"
    createContainer(tunnelName, tunnelImage, alias, networkId, 3334)
}

fun createService(index: Int, image: String, networkId: String) {
    val name = getServiceContainerName(index, image)
    val alias = "service"
    createContainer(name, image, alias, networkId, null)
}

private fun createContainer(name: String,
                            image: String,
                            alias: String,
                            networkId: String,
                            port: Int?): String {

    val existing = dockerClient
            .listContainersCmd()
            .withNameFilter(listOf(name))
            .withShowAll(true)
            .exec()
            .firstOrNull()

    if (existing != null) {
        if (existing.state == "running") return existing.id

        dockerClient.removeContainerCmd(existing.id)
                .withForce(true)
                .exec()
    }

    val hostConfig = HostConfig.newHostConfig()
            .withExtraHosts("host:$localIp")

    if (port != null) {
        val exposedPort = ExposedPort.tcp(port)
        val portBinding = PortBinding(Ports.Binding.bindPort(0), exposedPort)
        hostConfig.withPortBindings(portBinding)
    }

    val createCmd = dockerClient.createContainerCmd(image)
            .withName(name)
            .withHostConfig(hostConfig)

    if (port != null) {
        val exposedPort = ExposedPort.tcp(port)
        createCmd.withExposedPorts(exposedPort)
    }

    val create = createCmd.exec()

    dockerClient.connectToNetworkCmd()
            .withContainerId(create.id)
            .withNetworkId(networkId)
            .withContainerNetwork(ContainerNetwork().withAliases(alias))
            .exec()

    dockerClient.startContainerCmd(create.id)
            .exec()

    return create.id
}

fun createTunnelNetwork(index: Int, image: String): String {
    val networkName = getNetworkName(index, image)

    val existingNetwork = dockerClient.listNetworksCmd()
            .withNameFilter(networkName)
            .exec()
            .firstOrNull()

    if (existingNetwork != null) {
        return existingNetwork.id
    }

    val network = dockerClient.createNetworkCmd()
            .withDriver("bridge")
            .withCheckDuplicate(true)
            .withName(networkName)
            .exec()

    return network.id
}

fun getNetworkName(index: Int, image: String) = "nebula.bridge.$image.$index"
fun getTunnelContainerName(index: Int, image: String) = "nebula.tunnel.$image.$index"
fun getServiceContainerName(index: Int, image: String) = "nebula.service.$image.$index"


