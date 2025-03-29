package ru.lashnev.linemakerenvlinks.actions

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import ru.lashnev.linemakerenvlinks.utils.loadPluginConfig

class DynamicPopupActionGroup : ActionGroup(), DumbAware {
    private val config = loadPluginConfig()
    private val dynamicPopupsFactory = DynamicPopupsFactory(config)

    init {
        isPopup = true
    }

    override fun getChildren(event: AnActionEvent?): Array<AnAction> {
        event ?: return emptyArray()
        return dynamicPopupsFactory.createActions()
            .filter { action ->
                if (action is AbstractPopupAction) {
                    action.shouldBeEnabled(event)
                } else {
                    true
                }
            }
            .toTypedArray()
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isVisible = file != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}