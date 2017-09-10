package com.baulsupp.oksocial.completion

import com.google.common.collect.Lists.newArrayList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UrlListTest {
    @Test
    fun testReplacements() {
        val l = UrlList(UrlList.Match.EXACT,
                newArrayList("https://a.com/{location}", "https://a.com/here"))

        assertEquals(newArrayList("https://a.com/A", "https://a.com/B", "https://a.com/{location}",
                "https://a.com/here"), l.replace("location", newArrayList("A", "B"), true).getUrls(""))
    }

    @Test
    fun testReplacementsEmpty() {
        val l = UrlList(UrlList.Match.EXACT,
                newArrayList("https://a.com/{location}", "https://a.com/here"))

        assertEquals(newArrayList("https://a.com/{location}", "https://a.com/here"),
                l.replace("location", newArrayList(), true).getUrls(""))
    }
}
