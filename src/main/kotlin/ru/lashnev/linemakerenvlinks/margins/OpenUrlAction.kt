package ru.lashnev.linemakerenvlinks.margins

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import javax.swing.Icon

class OpenUrlAction(
    text: String,
    description: String,
    icon: Icon,
    private val url: String,
) : AnAction(text, description, icon) {
    override fun actionPerformed(e: AnActionEvent) {
        BrowserUtil.open(url)
    }
}
