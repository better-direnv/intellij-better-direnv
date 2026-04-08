<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# intellij-better-direnv Changelog

## [Unreleased]
 - Upgraded to latest intellij platform version, hopefully fixing most of the bugs in older versions

## [1.1.0]
 - Adds support for PHP run configurations (@shyim)
 - Fixes ShIcon not being found in 2021.3 (@shyim)
 - Fixes Python support (@jfreela)

## [1.0.0]
- Using an internal & experimental API to resolve an issue https://youtrack.jetbrains.com/issue/PY-56172/RunConfigurationpatchCommandLine-Not-Called-In-2022
resulting in a non-backwards compatible version of the plugin.

## [0.3.1]
- Fixes an issue with Python support in 2022.2 version of the IDE

## [0.3.0]
- Adds Python support
- Fixes NPE when direnv is enabled in a project but no .envrc is found

## [0.2.0]
- No longer looks for .envrc in working directory - instead lets direnv find it

## [0.1.1]
- Change how dynamic plugin loading of NodeJS works

## [0.1.0]
- Adds support for NodeJS run configurations

## [0.0.4]
- Add a plugin logo

## [0.0.3]
- Updated usage documentation in plugin description

## [0.0.2]
- Remove upward compatability range constraint

## [0.0.1]
### Added
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)