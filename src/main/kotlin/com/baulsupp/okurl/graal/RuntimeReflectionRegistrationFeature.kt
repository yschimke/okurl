package com.baulsupp.okurl.graal

import com.baulsupp.okurl.moshi.Rfc3339InstantJsonAdapter
import com.baulsupp.okurl.services.mapbox.model.MapboxLatLongAdapter
import com.oracle.svm.core.annotate.AutomaticFeature
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.github.classgraph.ClassGraph
import org.graalvm.nativeimage.hosted.Feature
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess
import org.graalvm.nativeimage.hosted.RuntimeReflection
import java.lang.reflect.ParameterizedType

@AutomaticFeature
internal class RuntimeReflectionRegistrationFeature : Feature {
  override fun beforeAnalysis(access: BeforeAnalysisAccess) {
    try {
      val pkg = "com.baulsupp.okurl"
      ClassGraph()
//      .verbose() // Log to stderr
        .enableAnnotationInfo()
        .enableMethodInfo()
        .enableClassInfo() // Scan classes, methods, fields, annotations
        .acceptPackages(pkg) // Scan com.xyz and subpackages (omit to scan all packages)
        .scan()
        .use { scanResult ->                    // Start the scan
          for (classInfo in scanResult.getSubclasses(JsonAdapter::class.java.name)) {
            registerMoshiAdapter(classInfo.loadClass())
          }
          for (classInfo in scanResult.getClassesWithMethodAnnotation(FromJson::javaClass.name)) {
            registerAnnotatedMoshiAdapter(classInfo.loadClass())
          }
        }
    } catch (e: Exception) {
      e.printStackTrace()
      throw e
    }

    // TODO move to block above
    registerAnnotatedMoshiAdapter(MapboxLatLongAdapter::class.java)
    registerAnnotatedMoshiAdapter(Rfc3339InstantJsonAdapter::class.java)
  }

  private fun registerMoshiAdapter(java: Class<*>) {
    RuntimeReflection.register(java)
    java.methods.forEach {
      RuntimeReflection.register(it)
    }
    val superclass = java.genericSuperclass as ParameterizedType
    // extends JsonAdapter<X>()
    val valueType = superclass.actualTypeArguments.first()
    if (valueType is Class<*>) {
      valueType.constructors.forEach {
        RuntimeReflection.register(it)
      }
    }
    try {
      RuntimeReflection.register(java.getConstructor(Moshi::class.java))
    } catch (nsme: NoSuchMethodException) {
      // expected
    }
  }

  private fun registerAnnotatedMoshiAdapter(java: Class<*>) {
    RuntimeReflection.register(java)
    java.declaredMethods.forEach {
      println(it)

      RuntimeReflection.register(it)
    }
  }
}
