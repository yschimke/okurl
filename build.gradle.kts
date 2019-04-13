import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.publish.maven.MavenPom
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version Versions.kotlin
  `maven-publish`
  distribution
  id("com.github.ben-manes.versions") version "0.21.0"
  id("com.jfrog.bintray") version "1.8.4"
  id("org.jetbrains.dokka") version "0.9.18"
  id("net.nemerosa.versioning") version "2.8.2"
  id("com.palantir.consistent-versions") version "1.5.0"
  id("com.diffplug.gradle.spotless") version "3.21.1"
}

repositories {
  mavenLocal()
  jcenter()
  mavenCentral()
  maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
  maven(url = "https://jitpack.io")
  maven(url = "http://repo.maven.apache.org/maven2")
  maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
  maven(url = "https://repo.spring.io/milestone/")
  maven(url = "https://dl.bintray.com/reactivesocket/RSocket/")
  maven(url = "https://oss.sonatype.org/content/repositories/releases/")
  maven(url = "https://dl.bintray.com/yschimke/baulsupp.com/")
}

group = "com.baulsupp"
val artifactID = "okurl"
description = "OkHttp Kotlin CLI"
val projectVersion: String = versioning.info.display
version = projectVersion

base {
  archivesBaseName = "okurl"
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
  withType(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.3"
    kotlinOptions.languageVersion = "1.3"
  }
}

tasks {
  withType(Tar::class) {
    compression = Compression.GZIP
  }

  withType<GenerateMavenPom> {
    destination = file("$buildDir/libs/${jar.get().baseName}.pom")
  }
}

tasks.named<DokkaTask>("dokka") {
  outputFormat = "javadoc"
  outputDirectory = "$buildDir/javadoc"
}

dependencies {
  implementation("javax.activation:activation")
  implementation("com.github.rvesse:airline")
  implementation("org.bouncycastle:bcprov-jdk15on")
  implementation("io.zipkin.brave:brave")
  implementation("io.zipkin.brave:brave-instrumentation-okhttp3")
  implementation("io.zipkin.brave:brave-okhttp")
  implementation("org.brotli:dec")
  implementation("com.jakewharton.byteunits:byteunits")
  implementation("commons-io:commons-io")
  implementation("org.apache.commons:commons-lang3")
  implementation("org.conscrypt:conscrypt-openjdk-uber")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor")
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.module:jackson-module-parameter-names")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation("org.fusesource.jansi:jansi")
  implementation("com.github.jnr:jffi")
  implementation("io.jsonwebtoken:jjwt-api")
  implementation("io.jsonwebtoken:jjwt-impl")
  implementation("io.jsonwebtoken:jjwt-jackson")
  implementation("pt.davidafsilva.apple:jkeychain")
  implementation("com.github.jnr:jnr-unixsocket")
  implementation("com.google.code.findbugs:jsr305")
  implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("com.squareup.moshi:moshi")
  implementation("com.squareup.moshi:moshi-adapters")
  implementation("com.squareup.moshi:moshi-kotlin")
  implementation("io.netty:netty-resolver-dns")
  implementation("com.github.mrmike:ok2curl")
  implementation("com.squareup.okhttp3:okhttp")
  implementation("com.baulsupp:okhttp-digest")
  implementation("com.squareup.okhttp3:okhttp-dnsoverhttps")
  implementation("com.squareup.okhttp3:logging-interceptor")
  implementation("com.squareup.okhttp3:okhttp-sse")
  implementation("com.squareup.okhttp3:okhttp-tls")
  implementation("com.squareup.okhttp3.sample:unixdomainsockets")
  implementation("com.squareup.okio:okio")
  implementation("com.baulsupp:oksocial-output")
  implementation("com.github.markusbernhardt:proxy-vole")
  implementation("org.slf4j:slf4j-api")
  implementation("org.slf4j:slf4j-jdk14")
  implementation("com.google.crypto.tink:tink")
  implementation("io.zipkin.java:zipkin")
  implementation("io.zipkin.reporter2:zipkin-sender-okhttp3")
  implementation("org.zeroturnaround:zt-exec")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug")

  implementation("org.jetbrains.kotlin:kotlin-script-util") {
    exclude(module = "kotlin-compiler")
  }

  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
  testImplementation("com.squareup.okhttp3:mockwebserver")
  testImplementation("org.conscrypt:conscrypt-openjdk-uber")

  testRuntime("org.junit.jupiter:junit-jupiter-engine")
  testRuntime("org.slf4j:slf4j-jdk14")
}

val sourcesJar by tasks.creating(Jar::class) {
  classifier = "sources"
  from(kotlin.sourceSets["main"].kotlin)
}

val javadocJar by tasks.creating(Jar::class) {
  classifier = "javadoc"
  from("$buildDir/javadoc")
}

val jar = tasks["jar"] as org.gradle.jvm.tasks.Jar

tasks.create("downloadDependencies") {
  description = "Downloads dependencies"

  doLast {
    configurations.forEach {
      if (it.isCanBeResolved) {
        it.resolve()
      }
    }
  }
}

fun MavenPom.addDependencies() = withXml {
  asNode().appendNode("dependencies").let { depNode ->
    configurations.implementation.get().allDependencies.forEach {
      depNode.appendNode("dependency").apply {
        appendNode("groupId", it.group)
        appendNode("artifactId", it.name)
        appendNode("version", it.version)
      }
    }
  }
}

publishing {
  publications {
    create("mavenJava", MavenPublication::class) {
      artifactId = artifactID
      groupId = project.group.toString()
      version = project.version.toString()
      description = project.description
      artifact(jar)
      artifact(sourcesJar) {
        classifier = "sources"
      }
      artifact(javadocJar) {
        classifier = "javadoc"
      }
      pom.addDependencies()
      pom {
        packaging = "jar"
        developers {
          developer {
            email.set("yuri@schimke.ee")
            id.set("yschimke")
            name.set("Yuri Schimke")
          }
        }
        licenses {
          license {
            name.set("Apache License")
            url.set("http://opensource.org/licenses/apache-2.0")
            distribution.set("repo")
          }
        }
        scm {
          connection.set("scm:git:https://github.com/yschimke/okurl.git")
          developerConnection.set("scm:git:git@github.com:yschimke/okurl.git")
          url.set("https://github.com/yschimke/okurl.git")
        }
      }
    }
  }
}

fun findProperty(s: String) = project.findProperty(s) as String?
bintray {
  user = findProperty("baulsuppBintrayUser")
  key = findProperty("baulsuppBintrayKey")
  publish = true
  setPublications("mavenJava")
  pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
    repo = "baulsupp.com"
    name = "okurl"
    userOrg = user
    websiteUrl = "https://github.com/yschimke/okurl"
    githubRepo = "yschimke/okurl"
    vcsUrl = "https://github.com/yschimke/okurl.git"
    desc = project.description
    setLabels("kotlin")
    setLicenses("Apache-2.0")
    version(delegateClosureOf<BintrayExtension.VersionConfig> {
      name = project.version.toString()
    })
  })
}

distributions {
  getByName("main") {
    contents {
      from("${rootProject.projectDir}") {
        include("README.md", "LICENSE")
      }
      from("${rootProject.projectDir}/src/main/scripts") {
        fileMode = Integer.parseUnsignedInt("755", 8)
        into("bin")
      }
      from("${rootProject.projectDir}/certificates") {
        into("certificates")
      }
      from("${rootProject.projectDir}/src/test/kotlin/commands") {
        fileMode = Integer.parseUnsignedInt("755", 8)
        exclude("local")
        into("bin")
      }
      from("${rootProject.projectDir}/bash") {
        into("bash")
      }
      from("${rootProject.projectDir}/zsh") {
        into("zsh")
      }
      from("${rootProject.projectDir}/src/main/resources") {
        into("scripts")
      }
      into("lib") {
        from(jar)
      }
      into("lib") {
        from(configurations.runtimeClasspath)
      }
    }
  }
}

val dependencyUpdates = tasks["dependencyUpdates"] as DependencyUpdatesTask
dependencyUpdates.resolutionStrategy {
  componentSelection {
    all {
      if (candidate.group == "io.netty" && candidate.version.startsWith("5.")) {
        reject("Alpha")
      }
    }
  }
}

spotless {
  kotlinGradle {
    ktlint("0.31.0").userData(mutableMapOf("indent_size" to "2", "continuation_indent_size" to "2"))
    trimTrailingWhitespace()
    endWithNewline()
  }
}
