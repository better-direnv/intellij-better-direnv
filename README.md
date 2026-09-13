# intellij-better-direnv

[![Build](https://github.com/better-direnv/intellij-better-direnv/actions/workflows/build.yml/badge.svg)](https://github.com/better-direnv/intellij-better-direnv/actions/workflows/build.yml)
[![Version](https://img.shields.io/jetbrains/plugin/v/19275-better-direnv.svg)](https://plugins.jetbrains.com/plugin/19275-better-direnv)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/19275-better-direnv.svg)](https://plugins.jetbrains.com/plugin/19275-better-direnv)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=better-direnv_intellij-better-direnv&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=better-direnv_intellij-better-direnv)

## Description

<!-- Plugin description -->
This plugin adds direnv support to IntelliJ IDEs.

The plugin can be accessed via the Run Configuration settings. To enable it for a given Run Configuration, check the
`Enable Direnv` checkbox. If you want it to automatically `direnv allow` when updates to the .envrc file are detected,
check the `Trust .direnv` checkbox. Note that this should only be done for trusted projects.

Currently supported Run Configurations:
  - Java
  - Go
  - NodeJS
  - Python
  - PHP
  - Ruby

Unfortunately, each run configuration type needs to be added manually. New run configuration support can be added
by request.

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "
  better_direnv"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/Fapiko/intellij-better-direnv/releases/latest) and install it
  manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Releasing

1. Update `CHANGELOG.md` — add your changes under the `[Unreleased]` section.
2. Bump the version in `gradle.properties`.
3. Push to `main` and confirm the CI build passes.
4. Create a GitHub release (as a pre-release or full release) targeting the desired tag via the GitHub UI or `gh` CLI.
   The [Release workflow](.github/workflows/release.yml) will trigger automatically and:
   - Patch `CHANGELOG.md` with the release body and open a PR to merge it back into `main`.
   - Publish the plugin to the JetBrains Marketplace.
   - Upload the built plugin as a release asset.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
