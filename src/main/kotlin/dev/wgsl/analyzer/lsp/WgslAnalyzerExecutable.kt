package dev.wgsl.analyzer.lsp

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.util.SystemInfo
import java.io.File

internal object WgslAnalyzerExecutable {
    private const val command = "wgsl-analyzer"

    fun find(): String? =
        PathEnvironmentVariableUtil.findInPath(command)?.absolutePath
            ?: cargoBinExecutable()?.absolutePath

    private fun cargoBinExecutable(): File? {
        val executableName = if (SystemInfo.isWindows) "$command.exe" else command
        val executable = File(System.getProperty("user.home"), ".cargo/bin/$executableName")
        return executable.takeIf(File::isFile)
    }
}
