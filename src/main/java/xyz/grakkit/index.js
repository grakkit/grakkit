const global = globalThis;
const server = org.bukkit.Bukkit.server;
const plugin = server.pluginManager.getPlugin('grakkit');

const core = {
   circular: function Circular () {},
   color: (text) => {
      return text.split('&').join('\xA7').split('\xA7\xA7').join('&');
   },
   command: (options) => {
      core.event('org.bukkit.event.server.TabCompleteEvent', (event) => {
         const input = event.buffer.slice(1).split(' ');
         if (options.name === input[0]) {
            event.completions = options.tabComplete(event.sender, ...input.slice(1));
         }
      });
      core.event('org.bukkit.event.player.PlayerCommandPreprocessEvent', (event) => {
         const input = event.message.slice(1).split(' ');
         if (options.name === input[0]) {
            options.execute(event.player, ...input.slice(1));
            event.cancelled = true;
         }
      });
   },
   data: (namespace, key) => {
      const store = core.store({ data: {}, [namespace]: {} });
      const file = core.folder(`${plugin.dataFolder}`, 'data', namespace).file(`${key}.json`);
      return store[key] || (store[key] = JSON.parse(file.read() || '{}'));
   },
   display: (object) => {
      if (object && object.constructor === core.circular) {
         return 'Circular';
      } else {
         const type = toString.apply(object);
         switch (type) {
            case '[object Object]':
            case '[object Function]':
            case '[foreign HostFunction]':
               return type.split(' ')[1].slice(0, -1);
            case '[object Array]':
               return `[ ${core.serialize(object).map(core.display).join(', ')} ]`;
            case '[foreign HostObject]':
               return `${object}` || `${object.class}`.split(' ').slice(1).join(' ');
            default:
               switch (typeof object) {
                  case 'function':
                     return 'Function';
                  case 'string':
                     return `"${object}"`;
                  case 'symbol':
                     return `@@${object.toString().slice(7, -1)}`;
                  default:
                     return `${object}`;
               }
         }
      }
   },
   event: (name, listener) => {
      const store = core.store({ event: {}, [name]: [] });
      if (store.push(listener) === 1) {
         server.pluginManager.registerEvent(
            eval(name).class,
            new (Java.extend(org.bukkit.event.Listener, {}))(),
            org.bukkit.event.EventPriority.HIGHEST,
            (info, data) => store.forEach((listener) => listener(data)),
            plugin
         );
      }
   },
   fetch: (location) => {
      return new Promise((resolve, reject) => {
         server.scheduler.scheduleAsyncDelayedTask(plugin, () => {
            const conn = new java.net.URL(location).openConnection();
            conn.doOutput = true;
            conn.requestMethod = 'GET';
            conn.instanceFollowRedirects = true;
            if (conn.responseCode === 200) {
               resolve({
                  stream: () => {
                     return conn.inputStream;
                  },
                  response: () => {
                     return new java.util.Scanner(conn.inputStream).useDelimiter('\\A').next();
                  }
               });
            } else {
               reject(conn.responseCode);
            }
         });
      });
   },
   file: (...nodes) => {
      const file = core.stat(...nodes).file();
      file.exists() || java.nio.file.Files.createFile(file.toPath());
      return {
         folder: () => {
            return core.folder(file.parentFile);
         },
         io: () => {
            return file;
         },
         path: () => {
            return core.stat(file.toPath().toString()).path();
         },
         read: () => {
            const output = [];
            const reader = new java.io.BufferedReader(new java.io.FileReader(file));
            reader.lines().forEach((line) => output.push(line));
            reader.close();
            return output.join('');
         },
         remove: (parent) => {
            file.delete();
            if (parent) {
               let context = file.parentFile;
               while (context.listFiles().length === 0) {
                  context.delete();
                  context = context.parentFile;
               }
            }
         },
         write: (data) => {
            const writer = new java.io.PrintWriter(new java.io.FileWriter(file));
            writer.print(data);
            writer.close();
         }
      };
   },
   folder: (...nodes) => {
      const stat = core.stat(...nodes);
      const path = stat.path();
      core.traverse([], path, {
         mode: 'array',
         post: (context) => {
            const file = core.stat(...context).file();
            file.exists() || file.mkdir();
         }
      });
      return {
         file: (name) => {
            return core.file(...path, name);
         },
         folder: (...nodes) => {
            return core.folder(...path, ...nodes);
         },
         io: () => {
            return stat.file();
         },
         path: () => {
            return stat.path();
         },
         remove: (parent) => {
            const clear = (folder) => {
               const files = folder.listFiles();
               if (files) {
                  for (let index = 0; index < files.length; ++index) {
                     const file = files[index];
                     file.directory ? clear(file) : file.delete();
                  }
               }
               folder.delete();
            };
            clear(stat.file());
            if (parent) {
               let context = stat.file().parentFile;
               while (context.listFiles().length === 0) {
                  context.delete();
                  context = context.parentFile;
               }
            }
         }
      };
   },
   keys: (object) => {
      return Object.getOwnPropertyNames(object);
   },
   serialize: (object, nullify, nodes) => {
      let output = null;
      if (object && typeof object === 'object') {
         nodes = nodes || [ object ];
         if (typeof object[Symbol.iterator] === 'function') {
            output = [];
            for (let entry of object) {
               if (nodes.includes(entry)) output.push(nullify ? null : new core.circular());
               else output.push(core.serialize(entry, nullify, [ ...nodes, entry ]));
            }
         } else {
            output = {};
            for (let entry in object) {
               if (nodes.includes(object[entry])) output[entry] = nullify ? null : new core.circular();
               else output[entry] = core.serialize(object[entry], nullify, [ ...nodes, object[entry] ]);
            }
         }
      } else {
         output = object;
      }
      return output;
   },
   stat: (...nodes) => {
      const path = java.nio.file.Path.of(...nodes);
      return {
         file: () => {
            return path.toFile();
         },
         path: () => {
            return [ ...path.toString().replace(/\\/g, '/').split('/') ];
         }
      };
   },
   store: (state) => {
      const db = core.store.db || (core.store.db = {});
      return core.traverse(db, core.keys(state), {
         mode: 'object',
         pre: (context, node) => {
            context[node] || (context[node] = state[node]);
         }
      });
   },
   text: (player, message, mode, color) => {
      color !== false && (message = core.color(message));
      switch (mode) {
         case 'action':
            return player.sendActionBar(message);
         case 'title':
            return player.sendTitle(...message.split('\n'));
         default:
            return player.sendMessage(message);
      }
   },
   traverse: (context, nodes, options) => {
      options || (options = {});
      for (let node of nodes) {
         options.pre && options.pre(context, node);
         switch (options.mode) {
            case 'string':
               context = context + node;
               break;
            case 'array':
               context.push(node);
               break;
            case 'object':
               context = context[node];
               break;
            case 'function':
               context = options.next(context, node);
               break;
         }
         options.post && options.post(context, node);
      }
      return context;
   }
};

const module = {
   apply: (source, current) => {
      return new Promise((resolve, reject) => {
         module
            .repo(source)
            .then((repo) => {
               repo
                  .release('latest')
                  .then((latest) => {
                     current && current === latest.data.id
                        ? reject('repository already up to date.')
                        : latest
                             .download()
                             .then((download) => {
                                const target = core.folder(`${plugin.dataFolder}`, 'modules', source);
                                target.remove();
                                java.nio.file.Files.move(
                                   download.folder.io().toPath(),
                                   target.io().toPath(),
                                   java.nio.file.StandardCopyOption.REPLACE_EXISTING
                                );
                                download.folder.remove(true);
                                resolve(latest.data.id);
                             })
                             .catch(() => {
                                reject('repository extraction failed.');
                             });
                  })
                  .catch(() => {
                     reject('no releases available.');
                  });
            })
            .catch(() => {
               reject('invalid repository.');
            });
      });
   },
   fetch: (source) => {
      return new Promise((resolve, reject) => {
         core
            .fetch(source)
            .then((output) => {
               const data = JSON.parse(output.response());
               return data.message ? reject(data.message) : resolve(data);
            })
            .catch((reason) => {
               reject(reason);
            });
      });
   },
   list: core.data('module', 'list', {}),
   repo: (source) => {
      const base = `https://api.github.com/repos/${source}`;
      return new Promise((resolve, reject) => {
         module
            .fetch(base)
            .then((data) => {
               resolve({
                  data: data,
                  release: (id) => {
                     return new Promise((resolve, reject) => {
                        module
                           .fetch(`${base}/releases/${id}`)
                           .then((data) => {
                              resolve({
                                 data: data,
                                 download: () => {
                                    return new Promise((resolve, reject) => {
                                       core
                                          .fetch(data.zipball_url)
                                          .then((output) => {
                                             let entry = null;
                                             let result = null;
                                             const stream = new java.util.zip.ZipInputStream(output.stream());
                                             const downloads = core.folder(`${plugin.dataFolder}`, 'downloads');
                                             while ((entry = stream.nextEntry)) {
                                                if (entry.directory) {
                                                   const folder = downloads.folder(entry.name);
                                                   result || (result = folder);
                                                } else {
                                                   const target = new java.io.FileOutputStream(
                                                      downloads.file(entry.name).io()
                                                   );
                                                   stream.transferTo(target);
                                                   target.close();
                                                }
                                                stream.closeEntry();
                                             }
                                             stream.close();
                                             resolve({ folder: result });
                                          })
                                          .catch((reason) => {
                                             reject(reason);
                                          });
                                    });
                                 }
                              });
                           })
                           .catch((reason) => {
                              reject(reason);
                           });
                     });
                  }
               });
            })
            .catch((reason) => {
               reject(reason);
            });
      });
   },
   require: (source) => {
      const installed = core.keys(module.list).includes(source);
      if (installed) {
         const folder = core.folder(`${plugin.dataFolder}`, 'modules', source);
         const data = JSON.parse(folder.file('package.json').read() || '{}');
         if (data.main) {
            try {
               return eval(folder.file(data.main).read());
            } catch (error) {
               console.trace(error);
               return 'script-error';
            }
         } else {
            return 'invalid-package';
         }
      } else {
         return 'package-unavailable';
      }
   },
   trusted: {}
};

const legacy = {
   server: server,
   __plugin: plugin,
   persist: (key) => {
      return core.data('legacy', key);
   },
   refresh: () => {
      server.pluginManager.disablePlugin(plugin);
      server.pluginManager.enablePlugin(plugin);
   },
   require: (source) => {
      return module.require(source);
   }
};

global.core = core;
global.global = global;
global.legacy = legacy;
global.module = module;
global.plugin = plugin;
global.server = server;

core.command({
   name: 'js',
   execute: (player, ...args) => {
      try {
         let output = null;
         const result = eval(args.join(' '));
         switch (toString.apply(result)) {
            case '[object Object]':
               const names = core.keys(result);
               output = `{ ${names.map((name) => `${name}: ${core.display(result[name])}`).join(', ')} }`;
               break;
            case '[object Function]':
               output = `${result}`.replace(/\r/g, '');
               break;
            case '[foreign HostFunction]':
               let input = args.slice(-1)[0].split('.').slice(-1)[0];
               input.endsWith(']') && (input = eval(input.replace(/.*\[/, '').slice(0, -1)));
               output = `hostFunction ${input}() { [native code] }`;
               break;
            default:
               output = core.display(result);
               break;
         }
         core.text(player, `&7${output}`);
      } catch (error) {
         const type = error.stack.split('\n')[0].split(' ')[0].slice(0, -1);
         switch (type) {
            case 'TypeError':
               core.text(player, `&c${type}: ${error.message.split('\n')[0]}`);
               break;
            case 'SyntaxError':
               core.text(player, `&c${type}: ${error.message.split(' ').slice(1).join(' ').split('\n')[0]}`);
               break;
         }
      }
   },
   tabComplete: (player, ...args) => {
      const input = args.slice(-1)[0];
      const filter = /.*(\!|\^|\&|\*|\(|\-|\+|\=|\[|\{|\||\;|\:|\,|\?|\/)/;
      const nodes = input.replace(filter, '').split('.');
      let context = global;
      let index = 0;
      while (index < nodes.length - 1) {
         let node = nodes[index];
         if (context[node]) {
            context = context[node];
            ++index;
         } else {
            index = Infinity;
         }
      }
      if (index === nodes.length - 1) {
         const segment = nodes.slice(-1)[0];
         return core
            .keys(context)
            .filter((key) => key.includes(segment))
            .map((comp) => (input.match(filter) || [ '' ])[0] + [ ...nodes.slice(0, -1), comp ].join('.'));
      } else {
         return [];
      }
   }
});

core.command({
   name: 'module',
   execute: (player, action, repo) => {
      if (action) {
         let prefix = `&7${action.toLowerCase()} $ `;
         if (repo) {
            repo = repo.toLowerCase();
            const source = module.trusted[repo] || repo.split('/').slice(-2).join('/');
            prefix += `&9${source} &f- `;
            const installed = core.keys(module.list).includes(source);
            switch (action.toLowerCase()) {
               case 'add':
                  if (installed) {
                     core.text(player, `${prefix}&crepository already installed.`);
                  } else {
                     core.text(player, `${prefix}&finstalling...`);
                     module
                        .apply(source)
                        .then((data) => {
                           module.list[source] = data;
                           core.text(player, `${prefix}&finstalled.`);
                        })
                        .catch((error) => {
                           core.text(player, `${prefix}&c${error}`);
                        });
                  }
                  break;
               case 'remove':
                  if (installed) {
                     core.text(player, `${prefix}&fdeleting...`);
                     try {
                        core.folder(`${plugin.dataFolder}`, 'modules', source).remove(true);
                        delete module.list[source];
                        core.text(player, `${prefix}&fdeleted.`);
                     } catch (error) {
                        core.text(player, `${prefix}&c${error}`);
                     }
                  } else {
                     core.text(player, `${prefix}&crepository not already installed.`);
                  }
                  break;
               case 'update':
                  if (installed) {
                     core.text(player, `${prefix}&fupdating...`);
                     try {
                        core.folder(`${plugin.dataFolder}`, 'modules', source).remove(true);
                        module
                           .apply(source, module.list[source])
                           .then((data) => {
                              module.list[source] = data;
                              core.text(player, `${prefix}&fupdated.`);
                           })
                           .catch((reason) => {
                              core.text(player, `${prefix}&c${reason}`);
                           });
                     } catch (error) {
                        core.text(player, `${prefix}&c${reason}`);
                     }
                  } else {
                     core.text(player, `${prefix}&crepository not installed.`);
                  }
                  break;
            }
         } else {
            core.text(player, `${prefix}&cno repository provided.`);
         }
      }
   },
   tabComplete: (player, action, repo, extra) => {
      if (repo === undefined) {
         action = action.toLowerCase();
         return [ 'add', 'remove', 'update' ].filter((value) => value.toLowerCase().contains(action));
      } else if (extra === undefined) {
         repo = repo.toLowerCase();
         switch (action) {
            case 'add':
               return core.keys(module.trusted).filter((value) => value.toLowerCase().contains(repo));
            case 'remove':
            case 'update':
               return core.keys(module.list).filter((value) => value.toLowerCase().contains(repo));
            default:
               return [];
         }
      } else {
         return [];
      }
   }
});

core.event('org.bukkit.event.server.PluginDisableEvent', (event) => {
   if (event.plugin === plugin) {
      const store = core.store({ data: {} });
      for (let namespace in store) {
         for (let key in store[namespace]) {
            const file = core.folder(`${plugin.dataFolder}`, 'data', namespace).file(`${key}.json`);
            file.write(JSON.stringify(core.serialize(store[namespace][key], true)));
         }
      }
   }
});

module.fetch('https://raw.githubusercontent.com/hb432/Grakkit/master/modules.json').then((data) => {
   module.trusted = data;
});
