package ru.lashnev.linemakerenvlinks.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiDocumentManager
import ru.lashnev.linemakerenvlinks.info.EmptyProjectInfo
import ru.lashnev.linemakerenvlinks.info.StandardProjectInfo
import ru.lashnev.linemakerenvlinks.utils.LinkGenerator
import ru.lashnev.linemakerenvlinks.utils.getFileNameWithPath
import ru.lashnev.linemakerenvlinks.utils.loadPluginConfig

abstract class BaseCustomAction : AnAction(), DumbAware {

    private val config = loadPluginConfig()
    private val linkGenerator = LinkGenerator()

    override fun actionPerformed(e: AnActionEvent) {
        val popupAction = config.popupActions.getOrNull(actionNumber())
        popupAction?.let {
            val project = e.project
            val editor = e.getData(CommonDataKeys.EDITOR)
            val psiFile = editor?.let {
                PsiDocumentManager.getInstance(project!!).getPsiFile(it.document)
            } ?: e.getData(CommonDataKeys.PSI_FILE)

            val projectInfo = e.project?.let { StandardProjectInfo(e.project!!) } ?: EmptyProjectInfo()
            BrowserUtil.browse(linkGenerator.replaceParameters(popupAction.openUrlAction.urlWithParameters, psiFile, projectInfo))
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        val presentation = e.presentation
        presentation.isVisible = false

        val popupAction = config.popupActions.getOrNull(actionNumber())
        popupAction?.let {
            val project = e.project
            val editor = e.getData(CommonDataKeys.EDITOR)
            val psiFile = editor?.let {
                PsiDocumentManager.getInstance(project!!).getPsiFile(it.document)
            } ?: e.getData(CommonDataKeys.PSI_FILE) ?: return

            if (popupAction.fileNameRegExp == null || Regex(popupAction.fileNameRegExp, RegexOption.IGNORE_CASE).matches(psiFile.getFileNameWithPath() ?: "")) {
                presentation.isVisible = true
                presentation.text = popupAction.openUrlAction.description
                presentation.icon = IconLoader.getIcon(popupAction.openUrlAction.icon, javaClass)
            } else {
                presentation.isVisible = false
            }
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    abstract fun actionNumber(): Int
}
