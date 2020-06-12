## Grakkit

It's the fusion of GraalVM and Bukkit.

[![Build Status](https://travis-ci.org/grakkit/grakkit.svg?branch=master)](https://travis-ci.org/grakkit/grakkit)

## Installation

You will need to run your server with GraalVM. Download one of the archives listed, extract the contents somewhere, and use `<graalvm>/bin/java` as your java path when launching a server, with `<graalvm>` referring to the directory to which you extracted GraalVM.

**GraalVM Download:** [Windows](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.0.0/graalvm-ce-java11-windows-amd64-20.0.0.zip) | [Mac](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.0.0/graalvm-ce-java11-darwin-amd64-20.0.0.tar.gz) | [Linux](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.0.0/graalvm-ce-java11-linux-amd64-20.0.0.tar.gz)

## Command: JS

The `js` command is used to evaluate code in-game. It tab-completes like a DevTools console. You can enable or disable **live evaluation** mode with `/grakkit eval enabled` and `/grakkit eval disabled`.

## Command: Module

The `module` command is the secret sauce of grakkit. Modules are added and updated via the GitHub API. The `/module add <repo>`, `/module update <repo>`, `/module remove <repo>`, and `/module list` commands serve as a "package manager" of sorts.

Generally, when checking for releases, `/module add` and `/module update` only consider standard releases. By setting your `/module channel` however, you can open this consideration up to include pre-releases as well.

Once you've installed a module, you can use `core.import('<repo>')` to access its content within other scripts. In any case, `<repo>` simply refers to a github repository, in `owner/repository` format.

## Scripts

Any files within the `scripts` folder will be executed on plugin load. This does not include sub-directories

## Modules

To create your own module, you can fork [this repository](https://github.com/grakkit/example) to get a head start. Since `/module add` and `/module update` use releases as download points, you will need to create at least one tag in your repository for use on servers.

# Want To Know More?
If you're looking to do anything major with Grakkit, this general overview isn't going to help you beyond getting started with the plugin. Head on over to [the wiki](https://github.com/grakkit/grakkit/wiki) for a more in-depth view of everything Grakkit has to offer.