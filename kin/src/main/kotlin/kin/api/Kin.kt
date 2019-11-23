package kin.api

object Kin {
    fun server(port: Int = 7777) = Server(port)
    fun client() = Client()
}