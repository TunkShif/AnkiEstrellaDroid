package one.tunkshif.ankiestrella.util

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TypeConverterTest {
    @Test
    fun mapToJson() {
        val map = mapOf(
            "A" to "a",
            "B" to "b",
        )
        val json = TypeConverter.mapToString(map)
        assertEquals(
            json,
            """
                {"A":"a","B":"b"}
            """.trimIndent()
        )
    }

    @Test
    fun jsonToMap() {
        val json = """
            {"A":"a","B":"b"}
        """.trimIndent()
        val map = TypeConverter.stringToMap(json)

        assertEquals(
            map,
            mapOf(
                "A" to "a",
                "B" to "b",
            )
        )
    }
}