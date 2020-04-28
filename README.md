## Grakkit

It's the fusion of GraalVM and Bukkit.

## Installation

You will need to run your server with GraalVM. Download one of the archives listed, extract the contents somewhere, and use `bin/java` as your java path when launching a server.

**GraalVM Download:** [Windows](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.0.0/graalvm-ce-java11-windows-amd64-20.0.0.zip) | [Mac](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.0.0/graalvm-ce-java11-darwin-amd64-20.0.0.tar.gz) | [Linux](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.0.0/graalvm-ce-java11-linux-amd64-20.0.0.tar.gz)

## Command: JS

The `js` command is used to evaluate code in-game. It tab-completes like a DevTools console.

## Command: Module

The `module` command is the secret sauce of grakkit.

To add a module to your server, use `/module add <repo>`. To remove a module, use `/module remove <repo>`. And, to update a module, use `/module update <repo>`.

In any case, `<repo>` can refer to one of the following:

-   A speed-dial keyword from the official module list.
-   A GitHub repository, in `username/repository` format.

## Modules

To create your own module, you can use [this repository](https://github.com/hb432/grakkit-test/) as a starting point.

You will need a GitHub repository with a `package.json`, which must contain a `main` field pointing to a JS file within the repository.

The `module` command makes use of your latest release for both `add` and `update` functionality, so you will need to publish your repository at least one time to use your module in a public environment.
