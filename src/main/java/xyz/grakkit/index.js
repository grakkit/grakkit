(function (global) {
   'use strict';

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
         const file = core.folder(core.root, 'data', namespace).file(`${key}.json`);
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
      eval: (µ, self) => {
         return eval(µ);
      },
      event: (name, listener) => {
         const store = core.store({ event: {}, [name]: [] });
         if (store.push(listener) === 1) {
            server.pluginManager.registerEvent(
               core.eval(name).class,
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
      from: (query, array) => {
         return array.filter((value) => value.contains(query));
      },
      keys: (object) => {
         return Object.getOwnPropertyNames(object);
      },
      lc: (string) => {
         return string.toLowerCase();
      },
      root: `${plugin.dataFolder}`,
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
      },
      values: (object) => {
         return core.keys(object).map((key) => {
            return object[key];
         });
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
                        if (current === latest.data.id) {
                           reject('repository already up to date.');
                        } else {
                           try {
                              core.folder(core.root, 'modules', source).remove(true);
                           } catch (error) {
                              reject('module folder could not be removed.');
                           }
                           latest
                              .download()
                              .then((download) => {
                                 const target = core.folder(core.root, 'modules', source);
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
                        }
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
      context: [ core.root ],
      download: (location) => {
         return new Promise((resolve, reject) => {
            core
               .fetch(location)
               .then((output) => {
                  let entry = null;
                  let result = null;
                  const stream = new java.util.zip.ZipInputStream(output.stream());
                  const downloads = core.folder(core.root, 'downloads');
                  while ((entry = stream.nextEntry)) {
                     if (entry.directory) {
                        const folder = downloads.folder(entry.name);
                        result || (result = folder);
                     } else {
                        const target = new java.io.FileOutputStream(downloads.file(entry.name).io());
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
      },
      exports: {},
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
      info: (repo) => {
         if (repo) {
            const source = module.trusted[repo] || repo.split('/').slice(-2).join('/');
            const folder = core.folder(core.root, 'modules', source);
            const data = JSON.parse(folder.file('package.json').read() || '{}');
            return {
               data: data,
               folder: folder,
               installed: core.keys(module.list).includes(source),
               js: data.main ? folder.file(data.main).read() : '',
               source: source,
               valid: data.main ? true : false
            };
         } else {
            const trusted = core.keys(module.trusted);
            return core.keys(module.list).map((key) => {
               for (let trustee of trusted) {
                  if (key === module.trusted[trustee]) {
                     return trustee;
                  }
               }
               return key;
            });
         }
      },
      list: core.data('module', 'list', {}),
      parse: (code, source) => {
         let result = undefined;
         const context = [ ...module.context ];
         module.context.push(...source.replace(/\\/g, '/').split('/'));
         if (!core.stat(...module.context).file().directory) module.context.pop();
         try {
            result = core.eval(code);
         } catch (error) {
            console.error(error);
         }
         module.context = context;
         module.exports = {};
         return result;
      },
      release: (location) => {
         return new Promise((resolve, reject) => {
            module
               .fetch(location)
               .then((data) => {
                  resolve({
                     data: data,
                     download: () => {
                        return module.download(data.zipball_url);
                     }
                  });
               })
               .catch((reason) => {
                  reject(reason);
               });
         });
      },
      repo: (source) => {
         const base = `https://api.github.com/repos/${source}`;
         return new Promise((resolve, reject) => {
            module
               .fetch(base)
               .then((data) => {
                  resolve({
                     data: data,
                     release: (id) => {
                        return module.release(`${base}/releases/${id}`);
                     }
                  });
               })
               .catch((reason) => {
                  reject(reason);
               });
         });
      },
      require: (source) => {
         if (source.startsWith('./')) {
            const data = core.folder(...module.context).file(source);
            return module.parse(data.read(), source);
         } else {
            const info = module.info(source);
            if (info.installed) {
               if (info.valid) {
                  return module.parse(info.js, `modules/${info.source}`);
               } else {
                  return 'invalid-package';
               }
            } else {
               return 'package-unavailable';
            }
         }
      },
      trusted: {}
   };

   const index = {
      core: core,
      get exports () {
         return module.exports;
      },
      set exports (value) {
         return (module.exports = value);
      },
      global: global,
      module: module,
      persist: (...args) => {
         return core.data('legacy', ...args);
      },
      plugin: plugin,
      refresh: (...args) => {
         server.pluginManager.disablePlugin(plugin);
         server.pluginManager.enablePlugin(plugin);
      },
      require: (...args) => {
         return module.require(...args);
      },
      server: server
   };

   if (global.exports) return (global.exports = index);

   core.command({
      name: 'js',
      execute: (player, ...args) => {
         try {
            let output = null;
            const result = core.eval(args.join(' '), player);
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
                  input.endsWith(']') && (input = core.eval(input.replace(/.*\[/, '').slice(0, -1)));
                  output = `hostFunction ${input}() { [native code] }`;
                  break;
               default:
                  output = core.display(result);
                  break;
            }
            core.text(player, `\u00a77${output}`, 'chat', false);
         } catch (error) {
            let type = 'Error';
            let message = `${error}`;
            if (error.stack) {
               type = error.stack.split('\n')[0].split(' ')[0].slice(0, -1);
               switch (type) {
                  case 'TypeError':
                     message = error.message.split('\n')[0];
                     break;
                  case 'SyntaxError':
                     message = error.message.split(' ').slice(1).join(' ').split('\n')[0];
                     break;
               }
            }
            core.text(player, `\u00a7c${type}: ${message}`, 'chat', false);
         }
      },
      tabComplete: (player, ...args) => {
         const input = args.slice(-1)[0];
         const filter = /.*(\!|\^|\&|\*|\(|\-|\+|\=|\[|\{|\||\;|\:|\,|\?|\/)/;
         const nodes = input.replace(filter, '').split('.');
         let context = Object.assign({ self: player }, global);
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
            action = core.lc(action);
            if ([ 'add', 'remove', 'update' ].includes(action)) {
               if (repo) {
                  repo = core.lc(repo).replace(/\\/g, '/');
                  let source = module.trusted[repo] || repo.split('/').slice(-2).join('/');
                  const installed = core.keys(module.list).includes(source);
                  switch (action) {
                     case 'add':
                        if (installed) {
                           core.text(player, `&7module $&e ${repo}&c repository already installed.`);
                        } else {
                           core.text(player, `&7module $&e ${repo}&f installing...`);
                           module
                              .apply(source)
                              .then((data) => {
                                 module.list[source] = data;
                                 core.text(player, `&7module $&e ${repo}&f installed.`);
                              })
                              .catch((error) => {
                                 core.text(player, `&7module $&e ${repo}&c ${error}`);
                              });
                        }
                        break;
                     case 'remove':
                        if (installed) {
                           core.text(player, `&7module $&e ${repo}&f deleting...`);
                           try {
                              core.folder(core.root, 'modules', source).remove(true);
                              module.list[source] = undefined;
                              core.text(player, `&7module $&e ${repo}&f deleted.`);
                           } catch (error) {
                              core.text(player, `&7module $&e ${repo}&c module folder could not be removed.`);
                           }
                        } else {
                           core.text(player, `&7module $&e ${repo}&c repository not already installed.`);
                        }
                        break;
                     case 'update':
                        if (installed) {
                           core.text(player, `&7module $&e ${repo}&f updating...`);
                           module
                              .apply(source, module.list[source])
                              .then((data) => {
                                 module.list[source] = data;
                                 core.text(player, `&7module $&e ${repo}&f updated.`);
                              })
                              .catch((error) => {
                                 core.text(player, `&7module $&e ${repo}&c ${error}`);
                              });
                        } else {
                           core.text(player, `&7module $&e ${repo}&c repository not installed.`);
                        }
                        break;
                  }
               } else {
                  core.text(player, `&7module $&c no repository specified.`);
               }
            } else {
               core.text(player, '&7module $&c invalid action.');
            }
         } else {
            core.text(player, '&7module $&c no action specified.');
         }
      },
      tabComplete: (player, action, repo, extra) => {
         if (repo === undefined) {
            return core.from(core.lc(action), [ 'add', 'remove', 'update' ]);
         } else if (extra === undefined) {
            switch (action) {
               case 'add':
                  return core.from(core.lc(repo), core.keys(module.trusted));
               case 'remove':
               case 'update':
                  return core.from(core.lc(repo), module.info());
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
               const file = core.folder(core.root, 'data', namespace).file(`${key}.json`);
               file.write(JSON.stringify(core.serialize(store[namespace][key], true)));
            }
         }
      }
   });

   module.fetch('https://raw.githubusercontent.com/hb432/Grakkit/master/modules.json').then((data) => {
      module.trusted = data;
   });

   const scripts = (folder) => {
      const files = folder.listFiles();
      if (files) {
         for (let index = 0; index < files.length; ++index) {
            const file = files[index];
            file.directory ? scripts(file) : core.eval(core.file(file.toPath().toString()).read());
         }
      }
   };

   scripts(core.folder(core.root, 'scripts').io());

   Object.assign(global, index);
})(globalThis);
