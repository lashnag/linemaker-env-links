package ru.lashnev.linemakerenvlinks.config

data class PluginConfig(
    var popupActions: List<PopupAction> = emptyList(),
    var yamlActions: List<YamlAction> = emptyList(),
    var mavenActions: List<MavenAction> = emptyList(),
    var methodCalledCodeActions: List<CodeAction> = emptyList(),
    var annotationCodeActions: List<CodeAction> = emptyList(),
    var interfaceImplementationActions: List<CodeAction> = emptyList(),
)

data class PopupAction(
    val fileNameRegExp: String? = null,
    val openUrlAction: OpenUrlAction,
)

data class YamlAction(
    val keyPathRegExp: String,
    val valueRegExp: String? = null,
    val marginAction: MarginAction,
)

data class MavenAction(
    val keyPathRegExp: String,
    val valueRegExp: String? = null,
    val marginAction: MarginAction,
)

data class CodeAction(
    val callerClass: String,
    val marginAction: MarginAction,
)

data class MarginAction(
    val openUrlAction: OpenUrlAction? = null,
    val multiplyOpenUrlAction: MultiplyOpenUrlAction? = null,
)

data class MultiplyOpenUrlAction(
    val description: String,
    val icon: String,
    val openUrlActions: List<OpenUrlAction>,
)

data class OpenUrlAction(
    val description: String,
    val icon: String,
    val urlWithParameters: String,
)