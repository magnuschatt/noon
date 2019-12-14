package nebula

fun main() {

    // start setty
    // start poky

    // for each container:
    // create network
    // create tunnel on network
    // create container on network

    // docker network create --driver bridge noonet
    // docker run --name setty -d -p 7788:7788 setty
    // docker run --add-host host:$(hostname -I | cut -f1 -d" ") --network noonet -d --name tunnel tunnel
    // docker run --network noonet poky

}