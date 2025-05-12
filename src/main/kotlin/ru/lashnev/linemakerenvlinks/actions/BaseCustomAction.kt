package ru.lashnev.linemakerenvlinks.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import ru.lashnev.linemakerenvlinks.config.OpenUrlAction
import ru.lashnev.linemakerenvlinks.info.EmptyProjectInfo
import ru.lashnev.linemakerenvlinks.info.StandardProjectInfo
import ru.lashnev.linemakerenvlinks.utils.LinkGenerator
import ru.lashnev.linemakerenvlinks.utils.getFileNameWithPath
import ru.lashnev.linemakerenvlinks.utils.loadPluginConfig

abstract class BaseCustomAction : ActionGroup(), DumbAware {

    private val config = loadPluginConfig()
    private val linkGenerator = LinkGenerator()

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val popupAction = config.popupActions.getOrNull(actionNumber()) ?: return emptyArray()

        return when {
            popupAction.multiplyOpenUrlAction != null -> {
                val actions = mutableListOf<AnAction>()
                actions.add(Separator.create())

                actions.addAll(
                    popupAction.multiplyOpenUrlAction.openUrlActions.map { action ->
                        createUrlAction(action)
                    }
                )

                actions.add(Separator.create())
                actions.toTypedArray()
            }
            popupAction.openUrlAction != null -> {
                val actions = mutableListOf<AnAction>()
                if (actionNumber() == 0) {
                    actions.add(Separator.create())
                }
                actions.add(createUrlAction(popupAction.openUrlAction))
                actions.toTypedArray()
            }
            else -> emptyArray()
        }
    }

    private fun createUrlAction(action: OpenUrlAction): AnAction {
        return object : AnAction(action.description, null, IconLoader.getIcon(action.icon, javaClass)) {
            override fun actionPerformed(e: AnActionEvent) {
                val psiFile = getPsiFile(e)
                val projectInfo = e.project?.let { StandardProjectInfo(it) } ?: EmptyProjectInfo()
                BrowserUtil.browse(linkGenerator.replaceParameters(action.urlWithParameters, psiFile, projectInfo))
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val popupAction = config.popupActions.getOrNull(actionNumber()) ?: run {
            e.presentation.isVisible = false
            return
        }

        val psiFile = getPsiFile(e) ?: run {
            e.presentation.isVisible = false
            return
        }

        val isVisible = Regex(popupAction.fileNameRegExp, RegexOption.IGNORE_CASE)
            .matches(psiFile.getFileNameWithPath() ?: "")

        e.presentation.isVisible = isVisible
        if (isVisible) {
            when {
                popupAction.multiplyOpenUrlAction != null -> {
                    e.presentation.text = popupAction.multiplyOpenUrlAction.description
                    e.presentation.icon = IconLoader.getIcon(popupAction.multiplyOpenUrlAction.icon, javaClass)
                }
                popupAction.openUrlAction != null -> {
                    e.presentation.text = popupAction.openUrlAction.description
                    e.presentation.icon = IconLoader.getIcon(popupAction.openUrlAction.icon, javaClass)
                }
            }
        }
    }

    private fun getPsiFile(e: AnActionEvent): PsiFile? {
        val editor = e.getData(CommonDataKeys.EDITOR)
        return editor?.let {
            PsiDocumentManager.getInstance(e.project!!).getPsiFile(it.document)
        } ?: e.getData(CommonDataKeys.PSI_FILE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    abstract fun actionNumber(): Int
}
