<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# intellij-better-direnv Changelog

## Unreleased

## 1.3.0 - 2026-04-09

- Upgraded to latest intellij platform version, hopefully fixing most of the bugs in older versions%0D%0A%0A## What's Changed%0D%0A* enable plugin verifier. output format by @maddingo in https://github.com/better-direnv/intellij-better-direnv/pull/49%0D%0A* Add tab title to run configuration editor by @cursive-ghost in https://github.com/better-direnv/intellij-better-direnv/pull/37%0D%0A* checkbox status not stored by @maddingo in https://github.com/better-direnv/intellij-better-direnv/pull/51%0D%0A* fix build badge, github token allows draft edit by @maddingo in https://github.com/better-direnv/intellij-better-direnv/pull/52%0D%0A%0A## New Contributors%0D%0A* @cursive-ghost made their first contribution in https://github.com/better-direnv/intellij-better-direnv/pull/37%0D%0A%0A**Full Changelog**: https://github.com/better-direnv/intellij-better-direnv/compare/v1.2.3-SNAPSHOT...v1.3.0

## 1.1.0

- Adds support for PHP run configurations (@shyim)
- Fixes ShIcon not being found in 2021.3 (@shyim)
- Fixes Python support (@jfreela)

## 1.0.0

- Using an internal & experimental API to resolve an issue https://youtrack.jetbrains.com/issue/PY-56172/RunConfigurationpatchCommandLine-Not-Called-In-2022
resulting in a non-backwards compatible version of the plugin.

## 0.3.1

- Fixes an issue with Python support in 2022.2 version of the IDE

## 0.3.0

- Adds Python support
- Fixes NPE when direnv is enabled in a project but no .envrc is found

## 0.2.0

- No longer looks for .envrc in working directory - instead lets direnv find it

## 0.1.1

- Change how dynamic plugin loading of NodeJS works

## 0.1.0

- Adds support for NodeJS run configurations

## 0.0.4

- Add a plugin logo

## 0.0.3

- Updated usage documentation in plugin description

## 0.0.2

- Remove upward compatability range constraint

## 0.0.1

### Added

- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
