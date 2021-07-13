![Project Logo](./logo.png)

**Graal Development Kit** - It's the fusion of GraalVM, JavaScript, and Minecraft.

![Code Demo](./demo.gif)

![Build Status](https://travis-ci.org/grakkit/grakkit.svg?branch=master)

If you want to get in touch with the community, join our [discord server](https://discord.gg/e682hwR) and we can assist you with whatever problem you may have.

# Getting Started

## Installation
Head on over to the [releases](https://github.com/grakkit/grakkit/releases) page and grab the latest version for your platform. After that, just drop the JAR in whatever **plugins**, **mods**, or **addons** folder you would install any other plugin, mod, or addon.

## Your First Project (Bukkit/Spigot/Paper)
Upon starting or reloading the server with the plugin installed, the `plugins/grakkit` folder will be created on your server. This is where most if not all development will take place, and serves as your home when working within the scope of grakkit.

Now, unless you know EXACTLY what you're doing (mad respect if you do) you should install the `@grakkit/server` package, a standard library for JavaScript development. Make sure you have NodeJS installed on your system, then navigate to `plugins/grakkit` in a terminal or command prompt and use `npm install @grakkit/server` to install it.

For those working on a remote server, you can simply install the `@grakkit/server` package to a local directory, then copy that directory's contents to the remote server's `plugins/grakkit` folder. This works because grakkit itself doesn't need NodeJS installed, it's merely the system we use for package management.

Once that's done, you can import it from within your main file (default `index.js`) as shown below...
```js
const stdlib = require('@grakkit/server');
```

...and upon the next server reload, the above code will be executed. This package also adds the in-game `/js` command, which can be used to test and execute code from within the game -- for example, `/js self` represents the player or console sending the command, and you can use `/js core.reload()` to reload the JS environment without having to reload the entire server.

## Your First Project (Fabric)
Grakkit for fabric is still in development. In the meantime, we recommend [KubeJS](https://kubejs.latvian.dev) as an alternative.

## Further Reading
For more info about Grakkit, modules, the JS command, and more, head on over to the [wiki](https://github.com/grakkit/grakkit/wiki) and read up. ***Attention:** This wiki is slightly outdated and really only applies to servers. Many of the code samples in here may still work, but some may not. Use at your own risk!*

For another useful guide to getting started, check out [Start Your Environment](https://github.com/grakkit/grakkit/wiki/HowTo-Start-Environment).
---

*Owned and maintained by [RepComm](https://github.com/RepComm) and [hb432](https://github.com/hb432). Special thanks to [brayjamin](https://github.com/brayjamin), [TonyGravagno](https://github.com/TonyGravagno), [dustinlacewell](https://github.com/dustinlacewell), [wagyourtail](https://github.com/wagyourtail), and [waterquarks](https://github.com/waterquarks) for their contributions to the project.*
