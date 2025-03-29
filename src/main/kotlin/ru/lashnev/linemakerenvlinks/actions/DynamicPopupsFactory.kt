package ru.lashnev.linemakerenvlinks.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.util.IconLoader
import ru.lashnev.linemakerenvlinks.config.PluginConfig

class DynamicPopupsFactory(private val config: PluginConfig) {
    fun createActions(): List<AnAction> {
        return config.popupActions.map { config ->
            object : AbstractPopupAction() {
                override fun actionPerformed(event: AnActionEvent) {
                    BrowserUtil.browse(config.urlWithParameters)
                }

                override fun shouldBeEnabled(event: AnActionEvent): Boolean {
                    return if (config.fileName != null) {
                        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
                        file?.name?.contains(config.fileName, ignoreCase = true) == true
                    } else {
                        true
                    }
                }

                override fun changePresentation(presentation: Presentation) {
                    presentation.text = config.name
                    presentation.icon = IconLoader.getIcon(config.icon, javaClass)
                }

                override fun getActionUpdateThread(): ActionUpdateThread {
                    return ActionUpdateThread.EDT
                }
            }
        }
    }
}