package kin.api

@FunctionalInterface
interface Handler {
    suspend fun handle(ctx: Context)
}