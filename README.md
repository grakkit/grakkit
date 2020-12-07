![Project Logo](./logo.png)

**Graal Development Kit** - It's the fusion of GraalVM, JavaScript, and Minecraft.

![Code Demo](./demo.gif)

![Build Status](https://travis-ci.org/grakkit/grakkit.svg?branch=master)

If you want to get in touch with the community, join our [discord server](https://discord.gg/e682hwR) and we can assist you with whatever problem you may have.

# Getting Started

## Installation
Head on over to the [releases](https://github.com/grakkit/grakkit/releases) page and grab the latest version for your platform. After that, just drop the JAR in whatever **plugins**, **mods**, or **addons** folder you would to install any other plugin, mod, or addon.

## Your First Project
Before creating a project, load your client or server. A **grakkit** folder should appear in your respective platform's config directory -- this folder will be the home of your new project.

The most basic project will consist of a single **index.js** file within the **grakkit** folder. You will need to create this file yourself. Once created, any code you type there will be executed on the next reload.

A project like that works, but it's just not the same as having the full suite of tooling available via NPM. If you don't already have NodeJS installed on your machine, do so now. Once installed, use `npm install @grakkit/server` or `npm install @grakkit/client` -- whatever best fits your current platform.

With that in play, add one of the following lines to your `index.js` file:
```js
const { core } = require('@grakkit/server'); /* for servers */
const { core } = require('@grakkit/client'); /* for clients */
```

...and reload the server. You now have full access to the core library. For more info about installing and using modules, head on over to the [wiki](https://github.com/grakkit/grakkit/wiki) and read up.

---

*Owned and maintained by [RepComm](https://github.com/RepComm) and [hb432](https://github.com/hb432). Special thanks to [TonyGravagno](https://github.com/TonyGravagno), [dustinlacewell](https://github.com/dustinlacewell), [wagyourtail](https://github.com/wagyourtail), and [waterquarks](https://github.com/waterquarks) for their contributions to the project.*