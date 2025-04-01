package ru.lashnev.linemakerenvlinks.margins

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.idea.base.utils.fqname.fqName
import org.jetbrains.kotlin.idea.core.receiverType
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructorCalleeExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import ru.lashnev.linemakerenvlinks.config.InfraAction
import ru.lashnev.linemakerenvlinks.config.PluginConfig
import ru.lashnev.linemakerenvlinks.info.ProjectInfo
import ru.lashnev.linemakerenvlinks.margins.markers.OpenUrlLineMarkerInfo
import ru.lashnev.linemakerenvlinks.margins.markers.RunLineMarkerInfo
import ru.lashnev.linemakerenvlinks.utils.LinkGenerator

class MarginsFactory(private val pluginConfig: PluginConfig) {

    private val linkGenerator = LinkGenerator()

    fun createMarkers(psiElement: PsiElement, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        if (psiElement is PsiIdentifier) {
            return parseJavaCode(psiElement, projectInfo)
        }

        if (psiElement is LeafPsiElement) {
            parseKotlinCode(psiElement, projectInfo)?.let { return it }
        }

        return null
    }

    private fun parseJavaCode(psiElement: PsiIdentifier, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        val method = psiElement.parentOfType<PsiMethodCallExpression>()
        if (method != null) {
            val reference = psiElement.parentOfType<PsiReferenceExpression>()
            if (reference != null) {
                val callerClass = reference.type?.internalCanonicalText
                return createByMethodCalledMarker(callerClass, psiElement, projectInfo)
            }

            return null
        }

        val annotation = psiElement.parentOfType<PsiAnnotation>()
        if (annotation != null) {
            val reference = psiElement.parentOfType<PsiJavaCodeReferenceElement>()
            if (reference != null) {
                val callerClass = reference.qualifiedName
                return createByAnnotationMarker(callerClass, psiElement, projectInfo)
            }
        }

        return null
    }

    private fun parseKotlinCode(psiElement: LeafPsiElement, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        when (psiElement.parent) {
            is KtNameReferenceExpression -> {
                val nameReference = psiElement.parent as KtNameReferenceExpression
                if (nameReference.parent is KtCallExpression) {
                    val method = nameReference.parent as KtCallExpression
                    val callerClass = method.receiverType()?.fqName?.asString()
                    return createByMethodCalledMarker(callerClass, psiElement, projectInfo)
                } else {
                    val constructor = psiElement.parentOfType<KtConstructorCalleeExpression>()
                    if (constructor != null) {
                        val annotation = constructor.parentOfType<KtAnnotationEntry>()
                        if (annotation != null) {
                            val reference = annotation.calleeExpression?.reference
                            val callerClass = when (val resolved = reference?.resolve()) {
                                is KtClassOrObject -> resolved.fqName?.asString()
                                is PsiClass -> resolved.qualifiedName
                                else -> return null
                            }
                            return createByAnnotationMarker(callerClass, psiElement, projectInfo)
                        }
                    }
                }
            }
        }

        return null
    }

    private fun createByMethodCalledMarker(callerClass: String?, psiElement: PsiElement, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        if (callerClass != null) {
            for (element in pluginConfig.infraByMethodCalled) {
                createMarker(callerClass, psiElement, element, projectInfo)?.let { return it }
            }
        }
        return null
    }

    private fun createByAnnotationMarker(callerClass: String?, psiElement: PsiElement, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        if (callerClass != null) {
            for (element in pluginConfig.infraByAnnotation) {
                createMarker(callerClass, psiElement, element, projectInfo)?.let { return it }
            }
        }
        return null
    }

    private fun createMarker(callerClass: String?, psiElement: PsiElement, element: InfraAction, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        if (callerClass?.startsWith(element.callerClass) == true) {
            element.openUrlAction?.let { openUrlAction ->
                return OpenUrlLineMarkerInfo(
                    element = psiElement,
                    icon = IconLoader.getIcon(openUrlAction.icon, javaClass),
                    tooltip = openUrlAction.description,
                    accessibleName = openUrlAction.description,
                    url = linkGenerator.replaceParameters(openUrlAction.urlWithParameters, psiElement, projectInfo),
                )
            }
            element.runAction?.let { runAction ->
                val actions = runAction.openUrlActions.map { openUrlAction ->
                    OpenUrlAction(
                        text = openUrlAction.description,
                        description = openUrlAction.description,
                        icon = IconLoader.getIcon(openUrlAction.icon, javaClass),
                        url = linkGenerator.replaceParameters(openUrlAction.urlWithParameters, psiElement, projectInfo),
                    )
                }

                return RunLineMarkerInfo(
                    psiElement,
                    icon = IconLoader.getIcon(runAction.icon, javaClass),
                    DefaultActionGroup(actions),
                    { runAction.description },
                    { runAction.description }
                )
            }
        }

        return null
    }
}
