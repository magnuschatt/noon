package kin.api

@FunctionalInterface
interface Handler {
    suspend fun handle(request: Request, response: ResponseWriter)
}