package ru.lashnev.linemakerenvlinks.config

data class PluginConfig(
    var popupActions: List<PopupAction> = emptyList(),
    var infraByMethodCalled: List<InfraAction> = emptyList(),
    var infraByAnnotation: List<InfraAction> = emptyList(),
)

data class PopupAction(
    val fileName: String? = null,
    val name: String,
    val icon: String,
    val urlWithParameters: String,
)

data class InfraAction(
    val callerClass: String,
    val openUrlAction: Action? = null,
    val runAction: RunAction? = null,
)

data class RunAction(
    val description: String,
    val icon: String,
    val openUrlActions: List<Action>,
)

data class Action(
    val description: String,
    val icon: String,
    val urlWithParameters: String,
)