package setty

import java.util.*

class Cache(val capacity: Int) {

    private val queue = LinkedList<String>()
    private val map = mutableMapOf<String, String>()
    val size: Int get() = map.size

    fun get(key: String): String? {
        if (queue.remove(key)) queue.addFirst(key)
        return map[key]
    }

    fun set(key: String, value: String) {
        map[key] = value
        queue.remove(key)
        queue.addFirst(key)

        if (queue.size > capacity) {
            map.remove(queue.removeLast())
        }
    }


}