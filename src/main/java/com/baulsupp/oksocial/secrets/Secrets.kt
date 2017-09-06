package com.baulsupp.oksocial.secrets

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.Writer
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.HashMap
import java.util.Optional
import java.util.Properties
import java.util.function.Function

import com.google.common.collect.Sets.newHashSet
import com.baulsupp.oksocial.output.util.FutureUtil.or
import java.util.Arrays.asList
import java.util.Optional.empty
import java.util.Optional.ofNullable
import java.util.stream.Collectors.joining

class Secrets(private val secrets: MutableMap<String, String>, private val file: Optional<Path>,
              private val defaults: Function<String, Optional<String>>) {
    private var changed = false

    operator fun get(key: String): Optional<String> {
        var result = ofNullable(secrets[key])

        if (!result.isPresent) {
            result = defaults.apply(key)
        }

        return result.filter { s -> !s.isEmpty() }
    }

    private fun put(key: String, value: String) {
        secrets.put(key, value)
        changed = true
    }

    @Throws(IOException::class)
    fun saveIfNeeded() {
        if (changed && file.isPresent) {
            val p = Properties()
            p.putAll(secrets)

            Files.newBufferedWriter(file.get()).use { w -> p.store(w, null) }
        }
    }

    companion object {
        private var instance: Secrets? = null

        fun loadSecrets(): Secrets {
            val classPathSecrets = loadClasspathDefaults()

            val configFile = FileSystems.getDefault().getPath(System.getenv("HOME"), ".oksocial-secrets.properties")

            val p = Properties()
            if (Files.exists(configFile)) {
                try {
                    Files.newBufferedReader(configFile).use { r -> p.load(r) }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            return Secrets(HashMap(p), Optional.of(configFile), Function { classPathSecrets[it] })
        }

        fun loadClasspathDefaults(): Secrets {
            val p = Properties()

            try {
                Secrets::class.java.getResourceAsStream("/oksocial-secrets.properties").use { `is` ->
                    if (`is` != null) {
                        p.load(`is`)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return Secrets(HashMap(p), empty(), { k -> empty<String>() })
        }

        fun getDefined(key: String): Optional<String> {
            return instance()[key]
        }

        fun prompt(name: String, key: String, defaultValue: String, password: Boolean): String {
            val defaulted = or(instance().getDefined(key)) { ofNullable(defaultValue) }

            val prompt = name + defaultDisplay(defaulted, password) + ": "

            var value = ""

            if (System.console() != null) {
                if (password) {
                    value = String(System.console().readPassword(prompt))
                } else {
                    value = System.console().readLine(prompt)
                }
            } else {
                System.err.println("using default value for " + key)
            }

            if (value.isEmpty()) {
                value = defaulted.orElse("")
            } else {
                instance().put(key, value)
            }

            return value
        }

        fun promptArray(name: String, key: String, defaults: Collection<String>): Set<String> {
            val valueString = prompt(name, key, defaults.stream().collect<String, *>(joining(",")), false)
            return newHashSet(asList(*valueString.split("\\s*,\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        }

        private fun defaultDisplay(defaultValue: Optional<String>, password: Boolean): String {
            var defaultValue = defaultValue
            if (password) {
                defaultValue = defaultValue.map { s -> s.replace(".".toRegex(), "\\*") }
            }

            return defaultValue.map { s -> " [$s]" }.orElse("")
        }

        @Synchronized
        fun instance(): Secrets {
            if (instance == null) {
                instance = loadSecrets()
            }

            return instance
        }
    }
}
