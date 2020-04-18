import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.publish.maven.MavenPom
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version Versions.kotlin
  `maven-publish`
  distribution
  id("com.github.ben-manes.versions") version "0.28.0"
  id("com.jfrog.bintray") version "1.8.5"
  id("org.jetbrains.dokka") version "0.10.1"
  id("net.nemerosa.versioning") version "2.12.1"
  id("com.diffplug.gradle.spotless") version "3.28.1"
  id("com.palantir.graal") version "0.6.0-120-g853647c"
  id("com.hpe.kraal") version "0.0.15"
}

repositories {
  mavenLocal()
  jcenter()
  mavenCentral()
  maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
  maven(url = "https://jitpack.io")
  maven(url = "https://repo.maven.apache.org/maven2")
  maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
  maven(url = "https://dl.bintray.com/kotlin/kotlin-dev/")
  maven(url = "https://repo.spring.io/milestone/")
  maven(url = "https://dl.bintray.com/reactivesocket/RSocket/")
  maven(url = "https://oss.sonatype.org/content/repositories/releases/")
  maven(url = "https://dl.bintray.com/yschimke/baulsupp.com/")
  maven(url = "https://packages.atlassian.com/maven-public")
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

    kotlinOptions.allWarningsAsErrors = false
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=enable")
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
  implementation("com.babylon.certificatetransparency:certificatetransparency:0.2.0")
  implementation("com.baulsupp:okhttp-digest:0.4.0")
  implementation("com.baulsupp:oksocial-output:4.34.0") {
    exclude(module = "svg-salamander")
    exclude(module = "jfreesvg")
  }
  implementation("com.fasterxml.jackson.core:jackson-databind:2.9.9.1")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.9.9")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.9.9")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.9")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")
  implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.9.9")
  implementation("com.github.jnr:jffi:1.2.20")
  implementation("com.github.jnr:jnr-unixsocket:0.23")
  implementation("com.github.markusbernhardt:proxy-vole:1.0.5")
  implementation("com.github.mrmike:ok2curl:0.5.0")
  implementation("com.google.code.findbugs:jsr305:3.0.2")
  implementation("com.google.crypto.tink:tink:1.2.2")
  implementation("com.jakewharton.byteunits:byteunits:0.9.1")
  implementation("com.squareup.moshi:moshi:1.8.0")
  implementation("com.squareup.moshi:moshi-adapters:1.8.0")
  implementation("com.squareup.moshi:moshi-kotlin:1.8.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.5.0")
  implementation("com.squareup.okhttp3:okhttp:4.5.0")
  implementation("com.squareup.okhttp3:okhttp-brotli:4.5.0")
  implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.5.0")
  implementation("com.squareup.okhttp3:okhttp-sse:4.5.0")
  implementation("com.squareup.okhttp3:okhttp-tls:4.5.0")
  implementation("com.squareup.okio:okio:2.4.3")
  implementation("commons-io:commons-io:2.6")
  implementation("info.picocli:picocli:4.2.0")
  implementation("io.jsonwebtoken:jjwt-api:0.10.6")
  implementation("io.jsonwebtoken:jjwt-impl:0.10.6")
  implementation("io.jsonwebtoken:jjwt-jackson:0.10.6")
  implementation("io.netty:netty-resolver-dns:4.1.48.Final")
  implementation("io.zipkin.brave:brave:5.7.0")
  implementation("io.zipkin.brave:brave-instrumentation-okhttp3:5.6.10")
  implementation("io.zipkin.brave:brave-okhttp:4.13.6")
  implementation("io.zipkin.java:zipkin:2.10.1")
  implementation("io.zipkin.reporter2:zipkin-sender-okhttp3:2.10.2")
  implementation("javax.activation:activation:1.1.1")
  implementation("org.apache.commons:commons-lang3:3.9")
  implementation("org.bouncycastle:bcprov-jdk15on:1.65")
  implementation("org.conscrypt:conscrypt-openjdk-uber:2.4.0")
  implementation("org.fusesource.jansi:jansi:1.18")
  implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.3.70")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.70")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.70")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.3.5")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.5")
  implementation("org.slf4j:slf4j-api:2.0.0-alpha0")
  implementation("org.slf4j:slf4j-jdk14:2.0.0-alpha0")
  implementation("org.zeroturnaround:zt-exec:1.11")
  implementation("pt.davidafsilva.apple:jkeychain:1.0.0")
  implementation("com.formdev:svgSalamander:1.1.2.1")
  implementation("org.jfree:jfreesvg:3.4")

  implementation("org.jetbrains.kotlin:kotlin-script-util:1.3.70") {
    exclude(module = "kotlin-compiler")
  }

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
  testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.70")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.3.70")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.5.0")
  testImplementation("org.conscrypt:conscrypt-openjdk-uber:2.4.0")

  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.6.2")
  testRuntime("org.slf4j:slf4j-jdk14:2.0.0-alpha0")
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

graal {
  graalVersion("20.0.0")
  mainClass("com.baulsupp.okurl.MainKt")
  outputName("okurl")
  option("--configurations-path")
  option("graal.config")
}

spotless {
  kotlinGradle {
    ktlint("0.31.0").userData(mutableMapOf("indent_size" to "2", "continuation_indent_size" to "2"))
    trimTrailingWhitespace()
    endWithNewline()
  }
}
