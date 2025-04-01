package ru.lashnev.linemakerenvlinks.utils

import ai.grazie.utils.capitalize
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethodCallExpression
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import ru.lashnev.linemakerenvlinks.info.ProjectInfo

class LinkGenerator {

    fun replaceParameters(link: String, projectInfo: ProjectInfo): String {
        var linkWithReplacedParameters = link
        ProjectInfo::class.java.methods.forEach { method ->
            val value = method.invoke(projectInfo).toString()
            val placeholder = "{${method.name}}"
            linkWithReplacedParameters = linkWithReplacedParameters.replace(placeholder, value)
        }
        return linkWithReplacedParameters
    }

    fun replaceParameters(link: String, psiElement: PsiElement, projectInfo: ProjectInfo): String {
        var linkWithReplacedParameters = link

        ProjectInfo::class.java.methods.forEach { method ->
            val value = method.invoke(projectInfo).toString()
            val placeholder = "{${method.name}}"
            linkWithReplacedParameters = linkWithReplacedParameters.replace(placeholder, value)
        }

        val parameters = when(psiElement) {
            is KtCallExpression -> getParametersFromArguments(psiElement)
            is PsiMethodCallExpression -> getParametersFromArguments(psiElement)
            else -> emptyList()
        }

        parameters.forEachIndexed { index, parameter ->
            val placeholder = "{parameter${index + 1}}"
            linkWithReplacedParameters = linkWithReplacedParameters.replace(placeholder, parameter)
        }

        val mapAnnotationParameters = when(psiElement) {
            is KtAnnotationEntry -> getAnnotationParameters(psiElement)
            is PsiAnnotation -> getAnnotationParameters(psiElement)
            else -> emptyMap()
        }

        mapAnnotationParameters.forEach { (key, value) ->
            linkWithReplacedParameters = linkWithReplacedParameters.replace("{annotation${key.capitalize()}}", value)
        }

        val andQueryStringParameters = parameters.joinToString(prefix = "\"", postfix = "\"", separator = "\" AND \"")
        linkWithReplacedParameters = linkWithReplacedParameters.replace("{andQueryStringParameters}", andQueryStringParameters)

        return linkWithReplacedParameters
    }

    private fun getParametersFromArguments(method: KtCallExpression): Collection<String> {
        val queries = mutableListOf<String>()
        method.valueArguments
            .firstOrNull()
            ?.getArgumentExpression()
            ?.accept(object : KtTreeVisitorVoid() {
                override fun visitLiteralStringTemplateEntry(entry: KtLiteralStringTemplateEntry) {
                    super.visitLiteralStringTemplateEntry(entry)
                    queries.addAll(
                        getQueriesFromJvmText(
                            entry.text
                        )
                    )
                }
            })

        return queries
    }

    private fun getParametersFromArguments(method: PsiMethodCallExpression): Collection<String> {
        val firstArgument = method
            .argumentList
            .expressions
            .firstOrNull()
        if (firstArgument is PsiLiteralExpression) {
            return getQueriesFromJvmText(
                firstArgument.text
            )
        }

        return emptyList()
    }

    private fun getQueriesFromJvmText(text: String): Collection<String> {
        return text.replace("\"", " ")
            .replace("!", "!!")
            .replace("'", "!'")
            .split("{}")
            .filterNot { it.isBlank() }
            .map { it.trim() }
    }

    private fun getAnnotationParameters(annotation: KtAnnotationEntry): Map<String, String> {
        val parameters = mutableMapOf<String, String>()

        annotation.valueArguments.forEach { argument ->
            val name = argument.getArgumentName()?.asName?.identifier ?: "value"
            val value = argument.getArgumentExpression()?.text
            parameters[name] = value.toString()
        }

        return parameters
    }

    private fun getAnnotationParameters(annotation: PsiAnnotation): Map<String, String> {
        val parameters = mutableMapOf<String, String>()

        annotation.parameterList.attributes.forEach { attribute ->
            val name = attribute.name ?: "value"
            val value = attribute.value?.text
            parameters[name] = value.toString()
        }

        return parameters
    }
}