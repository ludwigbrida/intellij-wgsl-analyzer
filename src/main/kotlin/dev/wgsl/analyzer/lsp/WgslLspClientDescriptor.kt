package dev.wgsl.analyzer.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.ProjectWideLspClientDescriptor

internal class WgslLspClientDescriptor(
    project: Project,
    private val executable: String,
) : ProjectWideLspClientDescriptor(project, "wgsl-analyzer") {
    override fun isSupportedFile(file: VirtualFile): Boolean =
        file.extension.equals("wgsl", ignoreCase = true) || file.extension.equals("wesl", ignoreCase = true)

    override fun getLanguageId(file: VirtualFile): String =
        if (file.extension.equals("wesl", ignoreCase = true)) "wesl" else "wgsl"

    override fun createCommandLine(): GeneralCommandLine = GeneralCommandLine(executable)

}
