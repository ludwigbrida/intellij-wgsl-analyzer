# intellij-wgsl-analyzer

A plugin for JetBrains IDEs that provides WGSL and WESL language support through the [`wgsl-analyzer`](https://github.com/wgsl-analyzer/wgsl-analyzer) language server.

> [!WARNING]
> This AI-generated slop exists mainly to support development of my own
> projects. It is a temporary stopgap to fill the void in WGSL/WESL tooling until
> something better comes along, and this repository may vanish at any time.

## Install

1. Download [dist/wgsl-analyzer.zip](dist/wgsl-analyzer.zip) directly from this repository.
2. In your JetBrains IDE, open **Settings | Plugins | ⚙ | Install Plugin from Disk…**.
3. Select the downloaded ZIP and restart the IDE when prompted.
4. Install [`wgsl-analyzer`](https://github.com/wgsl-analyzer/wgsl-analyzer) and make it available on `PATH` before opening a shader file.

The plugin checks `PATH` first and also checks Cargo's default installation location, `~/.cargo/bin`.

## Features

- Recognition, file icons, and lexical highlighting for `.wgsl` and `.wesl`
- Diagnostics, completion, definitions, formatting, signature help, hover, and inlay hints from `wgsl-analyzer`
- Continuation for line, block, and documentation comments

## WESL projects

Use `.wesl` for source that uses WESL syntax such as `import`, `package`, or `super`. A WESL project may import `.wgsl` modules, but the current `wgsl-analyzer` parses `.wgsl` source itself as standard WGSL.

For project-wide WESL imports, create `wesl.toml` in the project root:

```toml
edition = "2026_pre"
root = "./src"
```

`root` is the directory represented by `package::`. `include` is optional; by default WESL tools include `.wesl` and `.wgsl` files recursively below `root`.

## Compatibility

The plugin targets JetBrains Platform build 262 and newer, and is developed against WebStorm 2026.2.1.

## Troubleshooting

- If the IDE reports that `wgsl-analyzer` is missing, install it, add its executable directory to `PATH`, then reopen the shader file.
- To inspect language-server traffic, add `#com.intellij.platform.lsp` under **Help | Diagnostic Tools | Debug Log Settings**.

## Building from source

Building is only needed if you are contributing or creating a new release. Install a Java 21 JDK and make `java` available through `JAVA_HOME` or `PATH`.

```sh
./gradlew build
```

On Windows, use `gradlew.bat build`. The build task also runs `buildPlugin`, updating the installable ZIP at:

```text
dist/wgsl-analyzer.zip
```

To launch a development IDE, run `gradlew.bat runIde` on Windows or `./gradlew runIde` on macOS/Linux. The repository has no CI build pipeline. When publishing an update, build the ZIP locally and commit the updated `dist/wgsl-analyzer.zip` file.

## License

This software is provided under the [MIT license](./LICENSE.md).
