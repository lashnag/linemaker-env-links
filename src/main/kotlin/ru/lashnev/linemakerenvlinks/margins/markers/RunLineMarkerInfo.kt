package ru.lashnev.linemakerenvlinks.margins.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import java.util.function.Supplier
import javax.swing.Icon

class RunLineMarkerInfo(
    element: PsiElement,
    icon: Icon,
    private val myActionGroup: DefaultActionGroup,
    tooltipProvider: Function<in PsiElement, String>?,
    accessibleNameProvider: Supplier<String>,
) : LineMarkerInfo<PsiElement>(
    element,
    element.textRange,
    icon,
    tooltipProvider,
    null,
    GutterIconRenderer.Alignment.CENTER,
    accessibleNameProvider,
) {
    override fun createGutterRenderer(): GutterIconRenderer {
        return object : LineMarkerGutterIconRenderer<PsiElement>(this) {
            override fun getClickAction(): AnAction? {
                return null
            }

            override fun isNavigateAction(): Boolean {
                return true
            }

            override fun getPopupMenuActions(): ActionGroup {
                return myActionGroup
            }
        }
    }
}