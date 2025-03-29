package ru.lashnev.linemakerenvlinks.info

import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import org.jetbrains.plugins.gradle.util.GradleConstants

class StandardProjectInfo(project: Project) : ProjectInfo {
    private var appName: String = ""

    init {
        findPomFile(project)?.let {
            appName = getArtifactIdFromPom(project, it) ?: ""
        }

        findGradleFile(project)?.let {
            appName = getGradleProjectNameIntelliJ(project) ?: ""
        }
    }

    private fun findPomFile(project: Project) = project.guessProjectDir()
        ?.children
        ?.find { it.name == "pom.xml" }

    private fun findGradleFile(project: Project) = project.guessProjectDir()
        ?.children
        ?.find { it.name == "settings.gradle.kts" }

    override fun appName() = appName

    private fun getArtifactIdFromPom(project: Project, virtualFile: VirtualFile): String? {
        val psiFile: PsiFile? = PsiManager.getInstance(project).findFile(virtualFile)

        if (psiFile != null && psiFile is XmlFile) {
            psiFile.rootTag?.let { rootTag ->
                val artifactIdTag: XmlTag? = rootTag.subTags.firstOrNull { it.name == "artifactId" }
                return artifactIdTag?.value?.text
            }
        }

        return null
    }

    private fun getGradleProjectNameIntelliJ(project: Project): String? {
        val externalSystemId = GradleConstants.SYSTEM_ID
        val projectPath = project.basePath ?: return null

        val externalProjectInfo = ExternalSystemUtil.getExternalProjectInfo(project, externalSystemId, projectPath)

        val projectData = externalProjectInfo?.externalProjectStructure?.getData(ProjectKeys.PROJECT)

        return projectData?.externalName
    }
}
