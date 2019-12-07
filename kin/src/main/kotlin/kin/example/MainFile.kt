package kin.example

import kin.io.*
import kotlinx.coroutines.runBlocking
import java.io.File

fun main() = runBlocking {
    val reader = StringReader("this is some text now\n")
    val file = File("temp/xxx/file.txt")
    reader.copyTo(file.toWriter(append = false), close = false)

    println("File content: " + file.toReader().readToString())
    println("awd")

    Unit
}
