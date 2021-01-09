# q-intellij-plugin &middot; [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.txt) [![cloud build status](https://storage.googleapis.com/q-intellij-plugin/build/master-badge.svg)](https://github.com/a2ndrade/q-intellij-plugin)
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
- Remote code evaluation
- Remote function/global definition
- Custom authentication to remote Q instances

## Installation

1. Go to `Preferences` -> `Plugins`
1. Click on `Browse Repositories...` and select the `Languages` category
1. Look for the `q` plugin and click `Install`

## Contributing
### Getting Set Up

1. Make sure the `Grammar-Kit` and `PsiViewer` plugins are installed
1. Create a **fork** of this repository and **clone** it locally
1. Open the repository in IntelliJ via the "**Open**" option on the splash page
1. Open the Gradle tool window (`View` -> `Tool Windows` -> `Gradle`) and click on "**Refresh all Gradle Projects**"
1. From the command line, **run** `./gradlew compileJava compileTestJava`
1. **Build** the project (`Build` -> `Build Project`)

### Submitting Changes

1. Import and **use** the project [codestyle](codestyle.xml) (`Editor` -> `Code Style` -> `Scheme` -> `Import...`)
1. Push your work to a **branch** in your fork
1. Open a **Pull Request** and tag me (@a2ndrade)

