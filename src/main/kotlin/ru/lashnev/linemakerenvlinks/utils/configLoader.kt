package ru.lashnev.linemakerenvlinks.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.kotlinModule
import ru.lashnev.linemakerenvlinks.config.PluginConfig

fun loadPluginConfig(): PluginConfig {
    val mapper = ObjectMapper(YAMLFactory()).registerModule(kotlinModule())
    val inputStream = PluginConfig::class.java.classLoader.getResourceAsStream("config/plugin-config.yaml")
    return inputStream?.use {
        mapper.readValue(it, PluginConfig::class.java)
    } ?: throw IllegalStateException("Файл конфигурации не найден!")
}