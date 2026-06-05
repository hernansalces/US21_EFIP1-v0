# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A simple Java 26 project using IntelliJ IDEA as the build environment. No external build tool (Maven/Gradle) is configured.

## Technology Stack

- **Language**: Java 26
- **IDE**: IntelliJ IDEA (project defined via `US21_EFIP1.iml`)
- **Output**: `out/` directory (IntelliJ-managed)

## Building and Running

Build and run from within IntelliJ IDEA using the built-in Run action (`Shift+F10`).

To compile and run from the command line:
```powershell
javac --enable-preview --release 26 src/Main.java -d out
java --enable-preview -cp out Main
```

## Code Architecture

The project currently contains a single source file at `src/Main.java`. It uses Java 26's **unnamed classes and instance main methods** preview feature — there is no explicit `class` declaration, and `IO.println` is a preview API for console I/O. This allows top-level `void main()` without a surrounding class.