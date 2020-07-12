# Grakkit
**It's the fusion of GraalVM and Bukkit.**

[![Build Status](https://travis-ci.org/grakkit/grakkit.svg?branch=master)](https://travis-ci.org/grakkit/grakkit)

# Installation

## Pre-Requisite: GraalVM
You will need to run your server with GraalVM. Download one of the archives listed, extract the contents somewhere, and use `<graalvm>/bin/java` as your java path when launching a server, with `<graalvm>` referring to the directory to which you extracted GraalVM. If you're still confused, ask a developer on our [discord server](https://discord.gg/e682hwR) for help.

**GraalVM Download:** [Windows](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.0.0/graalvm-ce-java11-windows-amd64-20.0.0.zip) | [Mac](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.0.0/graalvm-ce-java11-darwin-amd64-20.0.0.tar.gz) | [Linux](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.0.0/graalvm-ce-java11-linux-amd64-20.0.0.tar.gz)

## Pre-Requisite: Paper (or any other system related to bukkit)

Download the latest version of Paper, move the JAR into your server's directory and point to it when launching the server. Again, you can join our [discord server](https://discord.gg/e682hwR) if you still need help setting things up.

*Note: While this plugin does work with Bukkit & Spigot, the enhanced performance of Paper in addition to many useful features in the API means that we recommend paper for the smooth operation of a server with Grakkit installed. For a more in-depth analysis, [click here](https://papermc.io).*

**Paper Download:** [https://papermc.io/downloads](https://papermc.io/downloads)

**Bukkit/Spigot Downloads:** [https://getbukkit.org](https://getbukkit.org)

## Plugin
Once you have GraalVM set up with your server, the installation process is just as simple as any other plugin installation. Just download or compile the Grakkit JAR and drop it into your server's plugins directory.

# Basics

## The User Script
Once you've installed the plugin, a `user.js` file will be created within `plugins/grakkit`. This file is your entry point to the world of JavaScript in Minecraft. You can use ES import/export syntax to bring other files into the mix.

## Custom Commands
Commands are created and registered with the `core.command` function. Feel free to use the examples below to get started with your own commands.

*Note: Commands will only show tab-completions if they are registered synchronously when the plugin loads.*
```javascript
// smite command (essentials)
// permission node: example.command.smite
core.command({
   name: 'smite',
   execute: (player, target) => {
      if (player.hasPermission('example.command.smite')) {
         if (target) {
            target = server.getPlayer(target);
            if (target) {
               target.getWorld().strikeLightning(target.getLocation());
               core.send(player, '§6Your target has been smitten.');
            } else {
               core.send(player, '§cThat player is offline or does not exist!');
            }
         } else {
            core.send(player, '§cUsage: /smite <target>');
         }
      } else {
         core.send(player, '§cInsufficient Permissions!');
      }
   },
   tabComplete: (player, target) => {
      return core.from(target, [ ...server.getOnlinePlayers() ].map((player) => player.getName()));
   }
});

// urban dictionary definition grabber
// permission node: (none)
core.command({
   name: 'urban',
   execute: (player, term) => {
      if (term) {
         player.sendMessage('§7Searching...');
         core.fetch(`https://api.urbandictionary.com/v0/define?term=${term}`, (response, error) => {
            if (response) {
               const json = response.json();
               if (json) {
                  const entry = json.list[0];
                  if (entry) {
                     core.send(player, `§6${entry.definition.split('\r\n')[0].replace(/[\[\]]/g, '')}`);
                  } else {
                     core.send(player, '§cThat word does not have a definition!');
                  }
               } else {
                  core.send(player, '§cAn error occured in the urban dictionary database!');
               }
            } else {
               core.send(player, `§cAn error (HTTP ${error}) occured!`);
            }
         });
      } else {
         core.send(player, '§cUsage: /urban <word>');
      }
   }
});
```

## Event Listeners
Event listeners are just that -- they listen for in-game events, like player deaths, entity spawns, etc. -- and execute code if and when those events take place.

For example, you could log useful information about a player to the console when they connect to the server:
```javascript
// detect player join
core.event('org.bukkit.event.player.PlayerJoinEvent', (event) => {
   const player = event.getPlayer();
   const location = player.getLocation();
   const world = location.getWorld().getName();
   const lines = [
      '-----------------------------------',
      `Name: ${player.getName()}`,
      `IP Address: ${player.getAddress().getHostName()}`,
      `Game Mode: ${player.getGameMode().toString()}`,
      `Location: { x: ${location.getX()}, y: ${location.getY()}, z: ${location.getZ()}, world: ${world} }`,
      `Health: ${player.getHealth()}/${player.getMaxHealth()}`,
      `Is Flying: ${player.isFlying() ? 'Yes' : 'No'}`,
      '-----------------------------------'
   ];
   lines.forEach((line) => console.log(line));
});
```

This event listener will allow players "retain" a certain percentage of their items upon death, with items to drop being chosen at random. `/gamerule keepInventory` must be enabled for this to work properly.
```javascript
const percentage = 30;
core.event('org.bukkit.event.player.PlayerRespawnEvent', (event) => {
   const player = event.getPlayer();
   const inventory = player.getInventory();
   inventory.setContents(
      [ ...inventory.getContents() ].map((item) => {
         if (percentage / 100 > Math.random()) {
            return item;
         } else {
            item && player.getWorld().dropItemNaturally(player.getLocation(), item);
            return null;
         }
      })
   );
});
```

## Persistent Data Storage
Let's say you want to store some data, and be able to access that data across a refresh, reload, or restart. The data function accepts a path-like string, such as the following:
```javascript
const data = core.data('example/data/path');
```
That `data` object is now linked to that path. When you refresh, reload, or restart the server, any data assigned to the object will be saved to a JSON file. If the data includes any circular references, they will be parsed out. If your data contains any value that is not an array, object, string, number, boolean, null, or undefined, it will also be parsed out.

In the following example, the `grakkit/jx` module is used store a player's inventory with JSON while they're offline. When they log in, the stored data is parsed back into their inventory.

```javascript
const $ = core.import('grakkit/jx');

// detect player join
$('*playerJoin').do((event) => {

   // get player data
   const player = $(event.getPlayer());
   const data = core.data(`player/${player.uuid()}/inventory`);

   // load inventory
   data.items && player.inventory($(data.items));
});

// detect player quit
$('*playerQuit').do((event) => {

   // get player data
   const player = $(event.getPlayer());
   const data = core.data(`player/${player.uuid()}/inventory`);

   // save inventory
   data.items = player.inventory().serialize();
});
```

# Modules

## Using Modules
Modules are the secret sauce of Grakkit. To add a module, use `/module add <repo>`. If needs be, you can update a module to the latest version with `/module update <repo>` or update all installed modules with `/module update *`. To remove a module, use `/module remove <repo>`.

Once you've added a module, you can use `core.import('<repo>')` to import it.

## Create Your Own
Modules are hosted on GitHub. You can fork [this repository](https://github.com/grakkit/example) to get a head start, or follow the example below.

In your `index.js` file, use the `core.export` function to export your module, like so:
```javascript
core.export('hello world');
```
Given the above code is hosted at the `grakkit/test` repository, the following will be true:
```javascript
core.import('grakkit/test') === 'hello world';
```

Now, the `core.export` function is ONLY used to export your module. In-module file loading is done with ES module syntax. Let's say you have another file in the same folder as `index.js`, for example, `crypto.js`:
```javascript
const SecureRandom = Java.type('java.security.SecureRandom');
const crypto = new SecureRandom();

export const random = () => {
   return (crypto.nextInt() + 2147483648) / 4294967296;
}
```
Given the above, you can use the following to import and call the `random` function from within the `index.js` file:
```javascript
import { random } from './crypto.js';

console.log(`A cryptographically-secure random number: ${random()}`);
```

# Miscellaneous

## A note about ScriptCraft
We have a module just for those who are looking to transition from ScriptCraft. Use `/module add grakkit/scriptcraft` to install it, and add the following at the top of your `user.js` file:
```javascript
core.import('grakkit/scriptcraft');
```
Once you've got that, reload the server. The scriptcraft directory should generate itself within your server's folder, and from that point on, things should work just like they did with ScriptCraft.

We fully respect and support ScriptCraft and what it did. For us, at least in part, Grakkit is a continuation of what ScriptCraft aimed to do, with plenty of our own ideas and features thrown in. We don't intend to start a rivalry between supporters of the two plugins.

## Legacy Support
Grakkit theoretically supports Minecraft versions going back to beta 1.4, given you have bukkit installed. This legacy support does not extend to modules, and likely never will. If you do intend to use grakkit on a beta or legacy release version, you may run into unexpected problems, however the `index.js` and `user.js` files should work properly in their default state.