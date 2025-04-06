package ru.lashnev.linemakerenvlinks.utils

import com.intellij.psi.PsiElement

fun PsiElement.getFileNameWithPath(): String? {
    val virtualFile = this.containingFile?.virtualFile ?: return null

    val projectBaseDir = this.project.basePath ?: return null
    val filePath = virtualFile.path

    if (!filePath.startsWith(projectBaseDir)) {
        return filePath
    }

    return filePath.substring(projectBaseDir.length + 1)
}