![Project Logo](./logo.png)

**Grakkit** - It's the fusion of GraalVM, JavaScript, and Minecraft.

![Code Demo](./demo.gif)

If you want to get in touch with the community, join our [discord server](https://discord.gg/e682hwR) and we can assist you with whatever problem you may have.

# Getting Started

## IMPORTANT
Grakkit was designed to run on standard JDK, which means it comes pre-packaged with the GraalJS engine! This pre-packaged engine WILL conflict with the GraalVM JVM, so please use standard JDK to run servers running Grakkit!

## Installation
Head on over to the [releases](https://github.com/grakkit/grakkit/releases) page and grab the latest version for your platform. After that, just drop the JAR in whatever **plugins**, **mods**, or **extensions** folder you would install any other plugin, mod, or extension.

## Your First Project (Bukkit/Spigot/Paper)
Upon starting or reloading the server with the plugin installed, the `plugins/grakkit` folder will be created on your server. This is where most if not all development will take place, and serves as your home when working within the scope of grakkit.

Given that Grakkit is community-driven, there are tons of NPM modules you can install to add rich support and typings for the game. For now, the most widely-used package is `@grakkit/stdlib-paper`, a standard library for JavaScript development with PaperMC servers. To install it, make sure you have NodeJS installed, then navigate to `plugins/grakkit` in a terminal or command prompt and use `npm install @grakkit/stdlib-paper`.

For those working on a remote server, you can simply install the package to a local directory, then copy that directory's contents to the remote server's `plugins/grakkit` folder. This works because grakkit itself doesn't need NodeJS installed, it's merely the template used for package management.

Once that's done, you can import it from within your main file (default `index.js`) as shown below...
```js
const stdlib = require('@grakkit/stdlib-paper');
```

Upon the next server reload, the above code will be executed. Requiring this package also adds the in-game `/js` command, which can be used to test and execute code from within the game -- for example, running `/js self` will show a representation of the player or console sending the command, and you can use `/js core.reload()` to reload the JS environment without having to reload the entire server.

## Your First Project (Minestom)
Grakkit for [Minestom](https://github.com/Minestom/Minestom) is available as a standalone "extension." Download it from our [latest release]() page. Make sure to download the correct platform, labeled `grakkit-x.y.z.minestom.jar`.
The installation process and usage is very similar to that of bukkit/spigot/paper's, however the two key differences are **a).** it's an _extension_ and should be placed in the extensions directory rather than plugins, and **b).** it uses different modules to complete the Environment™, notably-being `@grakkit/stdlib-minestom`.

Grakkit for Minestom is still in its early days, however it's the best we've got as far as scripting in a performant environment like Minestom.

## Other Platforms
Grakkit is currently only available for servers implementing the Bukkit or Minestom's API. Check out [Other Implementations](https://github.com/grakkit/grakkit/wiki/Other-Implementations) to find alternatives for your platform!

## Further Reading
For more info about Grakkit, modules, the JS command, and more, head on over to the [wiki](https://github.com/grakkit/grakkit/wiki) and read up. ***Attention:** This wiki is slightly outdated and only applies to servers. Many of the code samples in here may still work, but some may not. Use at your own risk!*

For another useful guide to getting started, check out [Start Your Environment](https://github.com/grakkit/grakkit/wiki/HowTo-Start-Environment).

## Check out these cool resources using Grakkit!
For even more goodies, check out the `#resources` or `#showcase` channel in our [Discord Server](https://discord.gg/e682hwR). 

###### Boilerplates
* [grakkit-boilerplate](https://github.com/MercerK/grakkit-boilerplate)

###### Libraries
* [grakkit-sound-browser](https://github.com/MercerK/grakkit-sound-browser)

###### Related Plugins
* [wscb-spigot (websocket)](https://github.com/RepComm/wscb-spigot)

###### Projects
* [crystalforest](https://github.com/RepComm/crystalforest)

###### Showcases
* [LibsDisguises Integration](https://gist.github.com/MercerK/9f793db326d03dddf6c7d09dc4e7be5d)
* [PlaceholderApi Integration](https://gist.github.com/MercerK/116d3be78ca43be1f71f4f4614597f5e)
* [Open Villager Inventory](https://gist.github.com/MercerK/473319ba8b47b4dcc10c5fea6a994442)
* [Persisting block data in a chunk](https://gist.github.com/MercerK/ed0b5721ddbb00ba0e38a0eb86363ef5)

###### Useful Gists
* [AnvilGUI Wrapper](https://gist.github.com/GodBleak/aea0d032c01e4f1cc3aef1a0e8d5c92b)

---

*Created by [RepComm](https://github.com/RepComm) and [spacefluff432](https://github.com/spacefluff432). Maintained by [brayjamin](https://github.com/brayjamin), [MercerK](https://github.com/MercerK),  [Mythical-Forest-Collective](https://github.com/Mythical-Forest-Collective), [spacefluff432](https://github.com/spacefluff432), [TonyGravagno](https://github.com/TonyGravagno), [dustinlacewell](https://github.com/dustinlacewell), [wagyourtail](https://github.com/wagyourtail), and [waterquarks](https://github.com/waterquarks).*
 
