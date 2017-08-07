# k-intellij-plugin
k/q language (kx.com) plugin for IntelliJ IDEA.

## Features

This plugin supports k3, k4 and q syntax. Features include:

- Syntax highlighting
- Navigate to declaration
- Code completion
- Find usages
- Rename refactoring
- File structure
- Go to symbol
- Color settings

## Building from sources

0. Configure an IntelliJ Platform SDK following [these instructions](http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html).
0. Make sure the `Grammar-Kit` and `PsiViewer` plugins are installed.
0. Clone this repository.
0. Open it as an IntelliJ IDEA project.
0. Open `src/main/resources/com/appian/intellij/k/k.flex` file and generate the lexer code (*)
0. Open `src/main/resources/com/appian/intellij/k/k.bnf` file and generate the parser code (*)
0. Build the project. Make sure the project is configured to use the IntelliJ Platform SDK configured in step 1.

(*) either from a context menu or using the keyboard shortcut ⇧⌘G

## Known Limitations
- k4/q block comments starting with `/` are not supported b/c of k3 incompatibility
