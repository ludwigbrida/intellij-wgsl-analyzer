package dev.wgsl.analyzer.lsp

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspIntegrationProvider

internal class WgslLspIntegrationProvider : LspIntegrationProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        clientStarter: LspIntegrationProvider.LspClientStarter,
    ) {
        if (!file.isWeslCompatibleSource()) return

        val executable = WgslAnalyzerExecutable.find()
        if (executable == null) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("wgsl-analyzer")
                .createNotification(
                    "wgsl-analyzer was not found. Install it and add it to PATH, then reopen the file.",
                    NotificationType.ERROR,
                )
                .notify(project)
            return
        }

        clientStarter.ensureClientStarted(WgslLspClientDescriptor(project, executable))
    }
}

private fun VirtualFile.isWeslCompatibleSource(): Boolean =
    extension.equals("wgsl", ignoreCase = true) || extension.equals("wesl", ignoreCase = true)
