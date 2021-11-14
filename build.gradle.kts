import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
  kotlin("jvm") version libs.versions.kotlin
  kotlin("kapt") version libs.versions.kotlin
  `maven-publish`
  application
  id("net.nemerosa.versioning") version "2.15.1"
  id("com.diffplug.spotless") version "5.1.0"
  id("com.palantir.graal") version "0.9.0"
}

application {
  mainClass.set("com.baulsupp.okurl.MainKt")
}

tasks.test {
  useJUnitPlatform()
}

repositories {
  mavenLocal()
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

group = "com.github.yschimke"
description = "OkHttp Kotlin CLI"
version = versioning.info.display

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
    kotlinOptions.apiVersion = "1.5"
    kotlinOptions.languageVersion = "1.5"

    kotlinOptions.allWarningsAsErrors = false
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=enable", "-Xopt-in=kotlin.RequiresOptIn")
  }
}

tasks {
  withType(Tar::class) {
    compression = Compression.NONE
  }
}

tasks {
  withType(Tar::class) {
    compression = Compression.NONE
  }
}

if (Os.isFamily(Os.FAMILY_MAC) || properties.containsKey("graalbuild")) {
  graal {
    mainClass("com.baulsupp.okurl.MainKt")
    outputName("okurl")
    graalVersion("21.3.0")

    (javaVersion as Property<String>).set("17")
//    javaVersion("16")

    option("--enable-https")
    option("--no-fallback")
    option("--allow-incomplete-classpath")

//  if (Os.isFamily(Os.FAMILY_WINDOWS)) {
//    // May be possible without, but autodetection is problematic on Windows 10
//    // see https://github.com/palantir/gradle-graal
//    // see https://www.graalvm.org/docs/reference-manual/native-image/#prerequisites
//    windowsVsVarsPath('C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\BuildTools\\VC\\Auxiliary\\Build\\vcvars64.bat')
//  }
  }
}

dependencies {
  implementation(platform("com.squareup.okhttp3:okhttp-bom:5.0.0-alpha.2"))

  api("com.github.yschimke:oksocial-output:6.5")
  api("com.squareup.moshi:moshi:1.12.0")
  api("com.squareup.moshi:moshi-adapters:1.12.0")
  api("com.squareup.moshi:moshi-kotlin:1.12.0")
  api("com.squareup.okhttp3:logging-interceptor")
  api("com.squareup.okhttp3:okhttp")
  api("com.squareup.okhttp3:okhttp-brotli")
  implementation("com.squareup.okhttp3:okhttp-dnsoverhttps")
  implementation("com.squareup.okhttp3:okhttp-sse")
  api("com.squareup.okhttp3:okhttp-tls")
  api("com.squareup.okio:okio:3.0.0")
  implementation("info.picocli:picocli:4.6.2")
  implementation("org.fusesource.jansi:jansi:2.4.0")
  implementation("io.jsonwebtoken:jjwt-api:0.11.2")
  implementation("io.jsonwebtoken:jjwt-impl:0.11.2")
  implementation("io.jsonwebtoken:jjwt-jackson:0.11.2")
  implementation("io.zipkin.brave:brave:5.13.3")
  implementation("io.zipkin.brave:brave-instrumentation-okhttp3:5.13.3")
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

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.1")

  compileOnly("org.graalvm.nativeimage:svm:21.3.0")
  kapt("info.picocli:picocli-codegen:4.6.2")

  kapt("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")
  implementation("io.github.classgraph:classgraph:4.8.131")

  implementation("io.swagger.parser.v3:swagger-parser:2.0.28")

  implementation("io.github.rburgst:okhttp-digest:2.5")

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
      artifact(tasks.distTar.get())
    }
  }
}

if (properties.containsKey("graalbuild")) {
  val nativeImage = tasks["nativeImage"]

  distributions {
    val graal = create("graal") {
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
