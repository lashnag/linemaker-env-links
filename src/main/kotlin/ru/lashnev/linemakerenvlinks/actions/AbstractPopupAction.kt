package ru.lashnev.linemakerenvlinks.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.DumbAwareAction

abstract class AbstractPopupAction : DumbAwareAction() {
    override fun update(event: AnActionEvent) {
        super.update(event)

        val presentation = event.presentation
        if (!shouldBeEnabled(event)) {
            presentation.isEnabledAndVisible = false
            return
        }
        changePresentation(presentation)
    }

    open fun shouldBeEnabled(event: AnActionEvent) = true
    open fun changePresentation(presentation: Presentation) {}
}
