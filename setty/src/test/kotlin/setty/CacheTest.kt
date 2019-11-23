package setty

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CacheTest {

    @Test
    fun `test cache stay fixed size`() {
        val cache = Cache(5)
        for (i in 1..100) {
            val key = i.toString()
            val value = Random.nextInt().toString()
            cache.set(key, value)
            assertEquals(value, cache.get(key), key)
            assertTrue { cache.size <= cache.capacity }
        }
        for (i in 1..100) {
            val key = i.toString()
            val value = Random.nextInt().toString()
            cache.set(key, value)
            assertEquals(value, cache.get(key))
            assertTrue { cache.size <= cache.capacity }
        }
        for (i in 1..100) {
            val key = i.toString()
            val value = Random.nextInt().toString()
            cache.set(key, value)
            assertEquals(value, cache.get(key))
            assertTrue { cache.size <= cache.capacity }
        }
    }

}