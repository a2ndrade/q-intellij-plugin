# q-intellij-plugin
q/k4 language (kx.com) [plugin](https://plugins.jetbrains.com/plugin/7925-q) for IntelliJ IDEA. For k3 support, see [k3-intellij-plugin](https://github.com/a2ndrade/k3-intellij-plugin)

## Features

This plugin supports q/k4 syntax. Features include:

- Syntax highlighting
- Navigate to declaration
- Code completion
- Find usages
- Rename refactoring
- File structure
- Go to symbol
- Color settings
- Code folding

## Installation

0. Go to `Preferences` -> `Plugins`
0. Click on `Browse Repositories...` and select the `Languages` category
0. Look for the `q` plugin and click `Install`

## Building from sources

0. Configure an IntelliJ Platform SDK following [these instructions](http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html).
0. Make sure the `Grammar-Kit` and `PsiViewer` plugins are installed.
0. Clone this repository.
0. Open it as an IntelliJ IDEA project.
0. Open `src/main/resources/com/appian/intellij/k/k.flex` file and generate the lexer code (*)
0. Open `src/main/resources/com/appian/intellij/k/k.bnf` file and generate the parser code (*)
0. Build the project. Make sure the project is configured to use the IntelliJ Platform SDK configured in step 1.

(*) either from a context menu or using the keyboard shortcut ⇧⌘G
