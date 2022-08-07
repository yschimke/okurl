import net.nemerosa.versioning.ReleaseInfo
import net.nemerosa.versioning.VersionInfo
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.charset.StandardCharsets

@Suppress("DSL_SCOPE_VIOLATION")

plugins {
  kotlin("jvm") version libs.versions.kotlin.get()
  kotlin("kapt") version libs.versions.kotlin.get()
  `maven-publish`
  application
  alias(libs.plugins.net.nemerosa.versioning)
  alias(libs.plugins.com.diffplug.spotless)
  alias(libs.plugins.com.palantir.graal)
  alias(libs.plugins.org.jreleaser)
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
      includeGroup("com.github.yschimke.schoutput")
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

sourceSets {
  main {
    java.srcDirs("$buildDir/generated/source/kotlinTemplates/main")
  }
}

val copyKotlinTemplates = tasks.register<Copy>("copyJavaTemplates") {
  from("src/main/kotlinTemplates")
  into("$buildDir/generated/source/kotlinTemplates/main")
  expand("projectVersion" to project.version)
  filteringCharset = StandardCharsets.UTF_8.toString()
}

tasks {
  withType(KotlinCompile::class) {
    kotlinOptions.apiVersion = "1.6"
    kotlinOptions.languageVersion = "1.6"

    kotlinOptions.allWarningsAsErrors = false
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=enable", "-Xopt-in=kotlin.RequiresOptIn")

    dependsOn(copyKotlinTemplates)
  }
}

graal {
  mainClass("com.baulsupp.okurl.MainKt")
  outputName("okurl")
  graalVersion("22.0.0.2")

  (javaVersion as Property<String>).set("17")

  option("--enable-https")
  option("--allow-incomplete-classpath")
  option("--no-fallback")
}

dependencies {
  implementation(platform("com.squareup.okhttp3:okhttp-bom:5.0.0-alpha.2"))

  api(libs.yschimke.schoutput)
  api(libs.moshi)
  api(libs.moshi.adapters)
  api(libs.moshi.kotlin)
  api(libs.okhttp3.logginginterceptor)
  api(libs.okhttp3.brotli)
  implementation(libs.okhttp3.dnsoverhttps)
  implementation(libs.okhttp3.sse)
  api(libs.okhttp3.tls)
  api(libs.okio)
  implementation(libs.picocli)
  implementation(libs.fusesource.jansi)
  implementation(libs.jsonwebtoken.jjwtapi)
  implementation(libs.jsonwebtoken.jjwtimpl)
  implementation(libs.jsonwebtoken.jjwtjackson)
  implementation(libs.zipkin.brave)
  implementation(libs.zipkin.braveinstrumentationokhttp3)
  implementation(libs.zipkin.braveokhttp)
  implementation(libs.zipkin.javazipkin)
  implementation(libs.zipkin.zipkinsenderokhttp3)
  implementation(libs.conscrypt.openjdkuber)
  implementation(libs.kotlin.reflect)
  api(libs.kotlin.stdlibjdk8)
  api(libs.kotlinx.coroutinescore)
  api(libs.kotlinx.coroutinesjdk8)
  implementation(libs.slf4j.api)
  implementation(libs.slf4j.jdk14)
  implementation(libs.davidafsilva.applejkeychain)
  api(libs.pgreze.kotlinprocess)

  testImplementation(libs.kotlin.test)
  testImplementation(libs.okhttp3.mockwebserver)

  testImplementation(libs.junit.jupiterapi)
  testImplementation(libs.junit.jupiterengine)

  compileOnly(libs.nativeimage.svm)
  kapt(libs.picocli.codegen)

  kapt(libs.moshi.kotlincodegen)
  implementation(libs.classgraph)

  implementation(libs.swagger.parser)

  implementation(libs.rburgst.okhttpdigest)

  testRuntimeOnly(libs.slf4j.jdk14)
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
      dependencies {
        dependency("jq")
      }
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

fun Project.booleanProperty(name: String) = findProperty(name).toString().toBoolean()

fun Project.booleanEnv(name: String) = System.getenv(name)?.toString().toBoolean()

task("tagRelease") {
  doLast {
    val tagName = versioning.info.nextVersion() ?: throw IllegalStateException("unable to compute tag name")
    exec {
      commandLine("git", "tag", tagName)
    }
    exec {
      commandLine("git", "push", "origin", "refs/tags/$tagName")
    }
  }
}

fun VersionInfo.nextVersion() = when {
  this.tag == null && this.branch == "main" -> {
    val matchResult = Regex("v(\\d+)\\.(\\d+)(?:.\\d+)").matchEntire(this.lastTag ?: "")
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
