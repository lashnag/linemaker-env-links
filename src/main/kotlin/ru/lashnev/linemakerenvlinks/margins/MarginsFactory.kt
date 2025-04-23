package ru.lashnev.linemakerenvlinks.margins

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import org.jetbrains.kotlin.idea.base.utils.fqname.fqName
import org.jetbrains.kotlin.idea.core.receiverType
import org.jetbrains.kotlin.psi.*
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLSequenceItem
import ru.lashnev.linemakerenvlinks.config.MarginAction
import ru.lashnev.linemakerenvlinks.config.PluginConfig
import ru.lashnev.linemakerenvlinks.info.ProjectInfo
import ru.lashnev.linemakerenvlinks.margins.markers.OpenUrlLineMarkerInfo
import ru.lashnev.linemakerenvlinks.margins.markers.RunLineMarkerInfo
import ru.lashnev.linemakerenvlinks.utils.LinkGenerator

class MarginsFactory(private val pluginConfig: PluginConfig) {

    private val linkGenerator = LinkGenerator()

    fun createMarkers(psiElement: PsiElement, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        if (psiElement is PsiIdentifier) {
            return getActionByJavaCode(psiElement, projectInfo)
        }

        if (psiElement is LeafPsiElement) {
            getActionByKotlinCode(psiElement, projectInfo)?.let { return it }
        }

        if (psiElement is YAMLKeyValue) {
            getActionByYaml(psiElement, projectInfo)?.let { return it }
        }

        if (psiElement is XmlTag) {
            getActionByXml(psiElement, projectInfo)?.let { return it }
        }

        return null
    }

    private fun getActionByJavaCode(psiElement: PsiIdentifier, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        val method = psiElement.parentOfType<PsiMethodCallExpression>()
        if (method != null) {
            val reference = psiElement.parentOfType<PsiReferenceExpression>()
            if (reference != null) {
                val callerClass = reference.type?.internalCanonicalText
                return callerClass?.let { createByMethodCalledMarker(callerClass, psiElement, projectInfo) }
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

    private fun getActionByKotlinCode(psiElement: LeafPsiElement, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        when (psiElement.parent) {
            is KtNameReferenceExpression -> {
                val nameReference = psiElement.parent as KtNameReferenceExpression
                if (nameReference.parent is KtCallExpression) {
                    val method = nameReference.parent as KtCallExpression
                    val callerClass = method.receiverType()?.fqName?.asString()
                    return callerClass?.let { createByMethodCalledMarker(callerClass, psiElement, projectInfo) }
                } else {
                    val constructor = psiElement.parentOfType<KtConstructorCalleeExpression>()
                    if (constructor != null) {
                        val annotation = constructor.parentOfType<KtAnnotationEntry>()
                        if (annotation != null) {
                            val annotationType = annotation.typeReference?.typeElement
                            val callerClass = when (annotationType) {
                                is KtUserType -> {
                                    val referencedName = annotationType.referenceExpression?.getReferencedNameAsName()?.asString()
                                    referencedName?.let { name ->
                                        annotation.containingKtFile.importDirectives.find {
                                            it.importedFqName?.shortName()?.asString() == name
                                        }?.importedFqName?.asString() ?: name
                                    }
                                }
                                else -> null
                            }
                            return callerClass?.let { createByAnnotationMarker(callerClass, psiElement, projectInfo) }
                        }
                    }
                }
            }
        }

        return null
    }

    private fun getActionByYaml(keyValue: YAMLKeyValue, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        val fullPath = getFullKeyPathWithIndices(keyValue)
        pluginConfig.yamlActions.forEach { yamlAction ->
            val pathMatch = Regex(yamlAction.keyPathRegExp, RegexOption.IGNORE_CASE).matches(fullPath)
            val valueMatch = yamlAction.valueRegExp?.let {
                Regex(yamlAction.valueRegExp, RegexOption.IGNORE_CASE).matches(keyValue.valueText)
            } ?: true
            if (pathMatch && valueMatch) {
                getMarginAction(keyValue, yamlAction.marginAction, projectInfo)?.let { return it }
            }
        }
        return null
    }

    private fun getActionByXml(xmlTag: XmlTag, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        val fullPath = getXmlPathWithIndices(xmlTag)
        pluginConfig.mavenActions.forEach { mavenAction ->
            println("Path: $fullPath")
            val keyFit = Regex(mavenAction.keyPathRegExp, RegexOption.IGNORE_CASE).matches(fullPath)
            val valueFit = mavenAction.valueRegExp == null || Regex(mavenAction.valueRegExp, RegexOption.IGNORE_CASE).matches(xmlTag.value.text)
            if (keyFit && valueFit) {
                getMarginAction(xmlTag, mavenAction.marginAction, projectInfo)?.let { return it }
            }
        }
        return null
    }

    private fun createByMethodCalledMarker(callerClass: String, psiElement: PsiElement, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        for (element in pluginConfig.methodCalledCodeActions) {
            if (callerClass.startsWith(element.callerClass)) {
                getMarginAction(psiElement, element.marginAction, projectInfo)?.let { return it }
            }
        }

        return null
    }

    private fun createByAnnotationMarker(callerClass: String, psiElement: PsiElement, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        for (element in pluginConfig.annotationCodeActions) {
            if (callerClass.startsWith(element.callerClass)) {
                getMarginAction(psiElement, element.marginAction, projectInfo)?.let { return it }
            }
        }

        return null
    }

    private fun getMarginAction(psiElement: PsiElement, marginAction: MarginAction, projectInfo: ProjectInfo): LineMarkerInfo<*>? {
        marginAction.openUrlAction?.let { openUrlAction ->
            return OpenUrlLineMarkerInfo(
                element = psiElement,
                icon = IconLoader.getIcon(openUrlAction.icon, javaClass),
                tooltip = openUrlAction.description,
                accessibleName = openUrlAction.description,
                url = linkGenerator.replaceParameters(openUrlAction.urlWithParameters, psiElement, projectInfo),
            )
        }
        marginAction.multiplyOpenUrlAction?.let { runAction ->
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

        return null
    }

    private fun getFullKeyPathWithIndices(keyValue: YAMLKeyValue): String {
        val pathParts = mutableListOf<String>()
        var current: PsiElement = keyValue

        while (true) {
            when (current) {
                is YAMLKeyValue -> {
                    pathParts.add(current.keyText)
                    current = current.parent
                }
                is YAMLSequenceItem -> {
                    val sequence = current.parent as? YAMLSequence ?: break
                    val index = sequence.items.indexOf(current)
                    pathParts.add(index.toString())
                    current = sequence.parent
                }
                is YAMLMapping -> {
                    current = current.parent
                }
                else -> break
            }
        }

        return pathParts
            .reversed()
            .joinToString(".") { part ->
                if (part.matches(Regex("\\d+"))) "[$part]" else part
            }
    }

    private fun getXmlPathWithIndices(tag: XmlTag): String {
        val pathParts = mutableListOf<String>()
        var currentTag: XmlTag? = tag

        while (currentTag != null) {
            val tagName = currentTag.name
            val parent = currentTag.parentTag

            val sameNamedSiblings = parent?.subTags
                ?.filter { it.name == tagName }
                ?: emptyList()

            val part = if (sameNamedSiblings.size > 1) {
                "$tagName.[${sameNamedSiblings.indexOf(currentTag)}]"
            } else {
                tagName
            }

            pathParts.add(part)
            currentTag = parent
        }

        return pathParts.asReversed().joinToString(".")
    }
}
