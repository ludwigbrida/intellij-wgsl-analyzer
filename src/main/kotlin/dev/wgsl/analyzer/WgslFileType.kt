package dev.wgsl.analyzer

import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object WgslFileType : LanguageFileType(WgslLanguage) {
    override fun getName(): String = "WGSL"

    override fun getDescription(): String = "WebGPU Shading Language"

    override fun getDefaultExtension(): String = "wgsl"

    override fun getIcon(): Icon = IconLoader.getIcon("/icons/wgsl.svg", WgslFileType::class.java)
}
