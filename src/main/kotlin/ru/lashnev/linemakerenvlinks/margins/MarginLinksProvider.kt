package ru.lashnev.linemakerenvlinks.margins

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import ru.lashnev.linemakerenvlinks.info.EmptyProjectInfo
import ru.lashnev.linemakerenvlinks.info.ProjectInfo
import ru.lashnev.linemakerenvlinks.info.StandardProjectInfo
import ru.lashnev.linemakerenvlinks.utils.loadPluginConfig
import java.util.concurrent.ConcurrentHashMap

class MarginLinksProvider : LineMarkerProvider {

    private val pluginConfig = loadPluginConfig()
    private val marginsFactory = MarginsFactory(pluginConfig)

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>,
    ) {
        super.collectSlowLineMarkers(elements, result)

        val projectInfo = elements.firstOrNull()?.let { psiElement ->
            projects.computeIfAbsent(psiElement.project) {
                StandardProjectInfo(it)
            }
        } ?: EmptyProjectInfo()

        elements.forEach { psiElement ->
            val marker = marginsFactory.createMarkers(psiElement, projectInfo)
            if (marker != null) {
                result.add(marker)
            }
        }
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        return null
    }
}

private val projects = ConcurrentHashMap<Project, ProjectInfo>()