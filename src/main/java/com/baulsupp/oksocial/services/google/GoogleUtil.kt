package com.baulsupp.oksocial.services.google

import com.google.common.collect.Sets
import java.util.Arrays
import java.util.Collections

object GoogleUtil {
    val SCOPES: Collection<String> = Arrays.asList("plus.login", "plus.profile.emails.read")

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.google.com")
    )

    fun fullScope(suffix: String): String {
        return if (suffix.contains("/")) suffix else "https://www.googleapis.com/auth/" + suffix
    }
}
