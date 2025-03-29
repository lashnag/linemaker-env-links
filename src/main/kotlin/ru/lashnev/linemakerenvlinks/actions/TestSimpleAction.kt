package ru.lashnev.linemakerenvlinks.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager

class TestSimpleAction : AnAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        // Основное действие, если нужно
        Messages.showMessageDialog(
            "Test Action Performed!",
            "Test Plugin",
            Messages.getInformationIcon()
        )
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        // Получаем текущее состояние действия, например, активность
        val presentation = e.presentation

        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = editor?.let {
            PsiDocumentManager.getInstance(project!!).getPsiFile(it.document)
        }

        // Динамически меняем текст действия
        val newText = psiFile?.name ?: "test"
        presentation.text = newText
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
