package com.baulsupp.oksocial.secrets

import com.google.common.collect.Sets.newHashSet
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.Arrays.asList

class Secrets(private val secrets: MutableMap<String, String>, private val file: Path?,
              private val defaults: (String) -> String?) {
    private var changed = false

    operator fun get(key: String): String? {
        var result = secrets[key] ?: defaults(key)

        return result?.let { if (it.isEmpty()) null else it }
    }

    private fun put(key: String, value: String) {
        secrets.put(key, value)
        changed = true
    }

    @Throws(IOException::class)
    fun saveIfNeeded() {
        if (changed && file != null) {
            val p = Properties()
            p.putAll(secrets)

            Files.newBufferedWriter(file).use { w -> p.store(w, null) }
        }
    }

    companion object {
        val instance: Secrets by lazy { loadSecrets() }

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

            return Secrets(p.toMutableStringMap(), configFile, { classPathSecrets[it] })
        }

        fun loadClasspathDefaults(): Secrets {
            val p = Properties()

            try {
                Secrets::class.java.getResourceAsStream("/oksocial-secrets.properties").use { s ->
                    if (s != null) {
                        p.load(s)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return Secrets(p.toMutableStringMap(), null, { _ -> null })
        }

        fun prompt(name: String, key: String, defaultValue: String, password: Boolean): String {
            val defaulted = instance[key] ?: defaultValue

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
                value = defaulted
            } else {
                instance.put(key, value)
            }

            return value
        }

        fun promptArray(name: String, key: String, defaults: Iterable<String>): List<String> {
            val valueString = prompt(name, key, defaults.joinToString(","), false)
            return valueString.split("\\s*,\\s*".toRegex()).dropLastWhile { it.isEmpty() }
        }

        private fun defaultDisplay(defaultValue: String, password: Boolean): String {
            var display = if (password) {
                defaultValue.replace(".".toRegex(), "\\*")
            } else {
                defaultValue
            }

            return " [$display]"
        }
    }
}

private fun Properties.toMutableStringMap(): MutableMap<String, String> =
        this.entries.associate { it.key.toString() to it.value.toString() }.toMutableMap()
