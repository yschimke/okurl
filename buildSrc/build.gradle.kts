plugins {
  `kotlin-dsl`
}

kotlinDslPluginOptions {
  experimentalWarning.set(false)
}

// Required since Gradle 4.10+.
repositories {
  jcenter()
  maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
}
