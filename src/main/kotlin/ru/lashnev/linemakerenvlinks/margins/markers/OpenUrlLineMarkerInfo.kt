package ru.lashnev.linemakerenvlinks.margins.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import java.awt.event.MouseEvent
import javax.swing.Icon

class OpenUrlLineMarkerInfo(
    element: PsiElement,
    icon: Icon,
    tooltip: String,
    accessibleName: String,
    url: String,
) : LineMarkerInfo<PsiElement>(
    element,
    element.textRange,
    icon,
    { tooltip },
    { _: MouseEvent, _: PsiElement -> BrowserUtil.browse(url) },
    GutterIconRenderer.Alignment.CENTER,
    { accessibleName },
)
