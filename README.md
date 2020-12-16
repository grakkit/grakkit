![Project Logo](./logo.png)

**Graal Development Kit** - It's the fusion of GraalVM, JavaScript, and Minecraft.

![Code Demo](./demo.gif)

![Build Status](https://travis-ci.org/grakkit/grakkit.svg?branch=master)

If you want to get in touch with the community, join our [discord server](https://discord.gg/e682hwR) and we can assist you with whatever problem you may have.

# Getting Started

## Installation
Head on over to the [releases](https://github.com/grakkit/grakkit/releases) page and grab the latest version for your platform. After that, just drop the JAR in whatever **plugins**, **mods**, or **addons** folder you would to install any other plugin, mod, or addon.

## Your First Project (Servers)
First, install the Grakkit plugin to your server. Upon starting or reloading the server, a **grakkit** folder will appear in your server's **plugins** directory, along with a **config.yml** file within. This config file contains the relative path of the entry point, which by default is **index.js**. If there is not already an **index.js** file within the **plugins/grakkit** directory, create it now.

With that in place, you can start installing some NPM modules. Most (if not all) servers will want to install the **@grakkit/server** package, which provides a standard library and environment for JS development in Minecraft. To install this package, use `npm install @grakkit/server`.

Once you've installed that, you can require it from within your index file like so:
```js
const { core } = require('@grakkit/server');
```

...and you will have full access to the **core** library after the next reload of the server. This library also includes the in-game **/js** command, which can be used to test and execute code from within the game -- for example, you can use `/js core.reload()` to reload the JS environment without having to reload the entire server, and `/js self` represents the player or console sending the command.

## Your First Project (Clients)
Grakkit for clients is still in development. In the meantime, we recommend [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs) as its infrastructure is already well-established.

## Further Reading
For more info about Grakkit, modules, the JS command, and more, head on over to the [wiki](https://github.com/grakkit/grakkit/wiki) and read up. **Note:** This wiki is currently outdated (we're working on updating it) and really only applies to servers. Many of the code samples in here may still work, but some may not. Use this at your own risk!

---

*Owned and maintained by [RepComm](https://github.com/RepComm) and [hb432](https://github.com/hb432). Special thanks to [TonyGravagno](https://github.com/TonyGravagno), [dustinlacewell](https://github.com/dustinlacewell), [wagyourtail](https://github.com/wagyourtail), and [waterquarks](https://github.com/waterquarks) for their contributions to the project.*
