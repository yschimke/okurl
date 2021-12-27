import net.nemerosa.versioning.ReleaseInfo
import net.nemerosa.versioning.VersionInfo
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.apache.tools.ant.taskdefs.condition.Os

@Suppress("DSL_SCOPE_VIOLATION")

plugins {
  kotlin("jvm") version libs.versions.kotlin
  kotlin("kapt") version libs.versions.kotlin
  `maven-publish`
  application
  id("net.nemerosa.versioning") version "2.15.1"
  id("com.diffplug.spotless") version "5.1.0"
  id("com.palantir.graal") version "0.10.0"
  id("org.jreleaser") version "0.9.1"
}

versioning {
  scm = "git"
  releaseParser = KotlinClosure2<net.nemerosa.versioning.SCMInfo, String, ReleaseInfo>({ scmInfo, _ ->
    if (scmInfo.tag != null && scmInfo.tag.startsWith("v")) {
      ReleaseInfo("release", scmInfo.tag.substring(1))
    } else {
      val parts = scmInfo.branch.split("/", limit = 2)
      ReleaseInfo(parts[0], parts.getOrNull(1) ?: "")
    }
  })
}

application {
  mainClass.set("com.baulsupp.okurl.MainKt")
}

tasks.test {
  useJUnitPlatform()
}

repositories {
  mavenCentral()
  maven {
    url = uri("https://jitpack.io")
    content {
      includeGroup("com.github.yschimke")
    }
  }
}

group = "com.github.yschimke"
description = "OkHttp Kotlin CLI"

version = versioning.info.effectiveVersion()

base {
  archivesName.set("okurl")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

kotlin {
  jvmToolchain {
    (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of("17"))
  }
}

tasks {
  withType(KotlinCompile::class) {
    kotlinOptions.apiVersion = "1.6"
    kotlinOptions.languageVersion = "1.6"

    kotlinOptions.allWarningsAsErrors = false
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=enable", "-Xopt-in=kotlin.RequiresOptIn")
  }
}

if (Os.isFamily(Os.FAMILY_MAC) || properties.containsKey("graalbuild")) {
  graal {
    mainClass("com.baulsupp.okurl.MainKt")
    outputName("okurl")
    graalVersion("21.3.0")

    (javaVersion as Property<String>).set("17")

    option("--enable-https")
    option("--no-fallback")
    option("--allow-incomplete-classpath")
  }
}

dependencies {
  implementation(platform("com.squareup.okhttp3:okhttp-bom:5.0.0-alpha.2"))

  api("com.github.yschimke:oksocial-output:6.5")
  api("com.squareup.moshi:moshi:1.13.0")
  api("com.squareup.moshi:moshi-adapters:1.13.0")
  api("com.squareup.moshi:moshi-kotlin:1.13.0")
  api("com.squareup.okhttp3:logging-interceptor")
  api("com.squareup.okhttp3:okhttp")
  api("com.squareup.okhttp3:okhttp-brotli")
  implementation("com.squareup.okhttp3:okhttp-dnsoverhttps")
  implementation("com.squareup.okhttp3:okhttp-sse")
  api("com.squareup.okhttp3:okhttp-tls")
  api("com.squareup.okio:okio:3.0.0") {
    version {
      strictly("3.0.0")
    }
  }
  implementation("info.picocli:picocli:4.6.2")
  implementation("org.fusesource.jansi:jansi:2.4.0")
  implementation("io.jsonwebtoken:jjwt-api:0.11.2")
  implementation("io.jsonwebtoken:jjwt-impl:0.11.2")
  implementation("io.jsonwebtoken:jjwt-jackson:0.11.2")
  implementation("io.zipkin.brave:brave:5.13.5")
  implementation("io.zipkin.brave:brave-instrumentation-okhttp3:5.13.5")
  implementation("io.zipkin.brave:brave-okhttp:4.13.6")
  implementation("io.zipkin.java:zipkin:2.10.1")
  implementation("io.zipkin.reporter2:zipkin-sender-okhttp3:2.16.3")
  implementation("org.conscrypt:conscrypt-openjdk-uber:2.5.2")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.0-RC")
  api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.0-RC")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2")
  implementation("org.slf4j:slf4j-api:2.0.0-alpha0")
  implementation("org.slf4j:slf4j-jdk14:2.0.0-alpha0")
  implementation("pt.davidafsilva.apple:jkeychain:1.0.0")
  api("com.github.pgreze:kotlin-process:1.3.1")

  testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.0-RC")
  testImplementation("com.squareup.okhttp3:mockwebserver")
  testImplementation("org.conscrypt:conscrypt-openjdk-uber:2.5.2")

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")

  compileOnly("org.graalvm.nativeimage:svm:21.3.0")
  kapt("info.picocli:picocli-codegen:4.6.2")

  kapt("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")
  implementation("io.github.classgraph:classgraph:4.8.138")

  implementation("io.swagger.parser.v3:swagger-parser:2.0.29")

  implementation("io.github.rburgst:okhttp-digest:2.6")

  testRuntimeOnly("org.slf4j:slf4j-jdk14:2.0.0-alpha0")
}

val sourcesJar by tasks.creating(Jar::class) {
  archiveClassifier.set("sources")
  from(kotlin.sourceSets["main"].kotlin)
}

val jar = tasks["jar"] as org.gradle.jvm.tasks.Jar

publishing {
  publications {
    create("mavenJava", MavenPublication::class) {
      from(components["java"])
      artifact(sourcesJar)
      artifact(tasks.distZip.get())
    }
  }
}

val nativeImage = tasks["nativeImage"]
val isJitpack = rootProject.booleanEnv("JITPACK")

if (!isJitpack) {
  distributions {
    create("graal") {
      contents {
        from("${rootProject.projectDir}") {
          include("README.md", "LICENSE")
        }
        from("${rootProject.projectDir}/zsh") {
          into("zsh")
        }
        into("bin") {
          from(nativeImage)
        }
      }
    }
  }
}

jreleaser {
  dryrun.set(rootProject.booleanProperty("jreleaser.dryrun"))

  project {
    website.set("https://github.com/yschimke/okurl")
    description.set("OkHttp Kotlin command line")
    authors.set(listOf("yschimke"))
    license.set("Apache-2.0")
    copyright.set("Yuri Schimke")
  }

  release {
    github {
      owner.set("yschimke")
      overwrite.set(true)
      skipTag.set(true)
    }
  }

  assemble {
    enabled.set(true)
  }

  packagers {
    brew {
      active.set(org.jreleaser.model.Active.RELEASE)
      addDependency("jq")
      repoTap {
        owner.set("yschimke")
        formulaName.set("okurl")
      }
    }
  }

  this.distributions.create("okurl") {
    active.set(org.jreleaser.model.Active.RELEASE)
    distributionType.set(org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE)
    artifact {
      platform.set("osx")
      path.set(file("build/distributions/okurl-graal-$version.zip"))
    }
  }
}

fun Project.booleanProperty(name: String) = this.findProperty(name).toString().toBoolean()

fun Project.booleanEnv(name: String) = (System.getenv(name) as String?).toString().toBoolean()

task("tagRelease") {
  val tagName = versioning.info.nextVersion() ?: throw IllegalStateException("unable to compute tag name")
  exec {
    commandLine("git", "tag", tagName)
  }
  exec {
    commandLine("git", "push", "origin", "refs/tags/$tagName")
  }
}

fun VersionInfo.nextVersion() = when {
  this.tag == null && this.branch == "main" -> {
    val matchResult = Regex("v(\\d+)\\.(\\d+)").matchEntire(this.lastTag ?: "")
    if (matchResult != null) {
      val (_, major, minor) = matchResult.groupValues
      "v$major.${minor.toInt() + 1}"
    } else {
      null
    }
  }
  else -> {
    null
  }
}

fun VersionInfo.effectiveVersion() = when {
  this.tag == null && this.branch == "main" -> {
    val matchResult = Regex("v(\\d+)\\.(\\d+)").matchEntire(this.lastTag ?: "")
    if (matchResult != null) {
      val (_, major, minor) = matchResult.groupValues
      "$major.${minor.toInt() + 1}.0-SNAPSHOT"
    } else {
      "0.0.1-SNAPSHOT"
    }
  }
  else -> {
    this.display
  }
}
