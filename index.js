(function () {
   'use strict';

   const global = globalThis;
   const server = org.bukkit.Bukkit.server;
   const plugin = server.pluginManager.getPlugin('grakkit');

   const core = {
      async: {
         clear: (task) => {
            return task.cancel();
         },
         immediate: (script) => {
            return server.scheduler.runTask(plugin, core.async.wrap(() => core.async.execute(script)));
         },
         interval: (script, delay, interval) => {
            return server.scheduler.runTaskTimer(
               plugin,
               core.async.wrap(() => core.async.execute(script)),
               delay / 50,
               interval / 50
            );
         },
         execute: (script) => {
            return server.scheduler.runTaskAsynchronously(plugin, core.async.wrap(script));
         },
         timeout: (script, delay) => {
            return server.scheduler.runTaskLater(plugin, core.async.wrap(() => core.async.execute(script)), delay / 50);
         },
         wrap: (script) => {
            return new java.lang.Runnable({ run: () => script() });
         }
      },
      circular: function Circular () {},
      clear: (folder) => {
         const files = folder.listFiles();
         if (files) {
            for (let index = 0; index < files.length; ++index) {
               const file = files[index];
               file.directory ? core.clear(file) : file.delete();
            }
         }
         folder.delete();
      },
      color: (text) => {
         return text.split('&').join('\xA7').split('\xA7\xA7').join('&');
      },
      command: (options) => {
         const name = options.name;
         const input = Object.assign(
            {
               prefix: 'grakkit',
               usage: `/${name} <...args>`,
               description: 'A Minecraft command',
               execute: () => {},
               tabComplete: () => []
            },
            options
         );
         core.commands[name] = { execute: input.execute, tabComplete: input.tabComplete };
         const prefix = `(player,args)=>core.commands[${JSON.stringify(name)}]`;
         const suffix = "(player,...args.split(' '))";
         const status = plugin.register(
            input.prefix,
            input.name,
            input.usage,
            input.description,
            `${prefix}.execute${suffix}`,
            `${prefix}.tabComplete${suffix}`
         );
         return status ? name : `${prefix}:${name}`;
      },
      commands: {},
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
                  const output = `${object}`;
                  if (!output || output.startsWith('class ')) {
                     return object.canonicalName || object.class.canonicalName;
                  } else {
                     return output;
                  }
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
      ensure: (path) => {
         core.traverse([], path, {
            mode: 'array',
            post: (context) => {
               const file = core.stat(...context).file();
               file.exists() || file.mkdir();
            }
         });
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
            core.async.immediate(() => {
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
         return {
            folder: () => {
               return core.folder(file.parentFile);
            },
            io: () => {
               return file;
            },
            exists: () => {
               return file.exists();
            },
            path: () => {
               return core.stat(file.toPath().toString()).path();
            },
            read: () => {
               if (file.exists()) {
                  const output = [];
                  const reader = new java.io.BufferedReader(new java.io.FileReader(file));
                  reader.lines().forEach((line) => output.push(line));
                  reader.close();
                  return output.join('');
               } else {
                  return '';
               }
            },
            remove: (parent) => {
               file.exists() && file.delete();
               if (parent && file.parentFile.exists()) {
                  let context = file.parentFile;
                  while (context.listFiles().length === 0) {
                     context.delete();
                     context = context.parentFile;
                  }
               }
            },
            write: (data) => {
               core.ensure(file.parentFile.path.toString().replace(/\\/g, '/').split('/'));
               file.exists() || java.nio.file.Files.createFile(file.toPath());
               const writer = new java.io.PrintWriter(new java.io.FileWriter(file));
               writer.print(data);
               writer.close();
            }
         };
      },
      folder: (...nodes) => {
         const stat = core.stat(...nodes);
         const path = stat.path();
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
            exists: () => {
               return stat.file().exists();
            },
            path: () => {
               return stat.path();
            },
            remove: (parent) => {
               stat.file().exists() && core.clear(stat.file());
               if (parent && stat.file().parentFile.exists()) {
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
               .repo(source.slice(1))
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
                              reject('repo folder could not be removed.');
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
      default: {
         index: 'module.exports = (function (global) {\n   return {\n      /* exports */\n   }\n})(globalThis);\n',
         package: '{\n   "main": "./index.js"\n}\n'
      },
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
            const source = module.source(repo);
            const folder = core.folder(core.root, 'modules', source);
            const data = JSON.parse(folder.file('package.json').read() || '{}');
            const script = data.main ? folder.file(data.main) : null;
            return {
               data: data,
               folder: folder,
               installed: core.keys(module.list).includes(source),
               js: script ? script.read() : null,
               script: script,
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
      list: core.data('grakkit', 'modules', {}),
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
            const script = core.folder(...module.context).file(source);
            console.log(`evaluating script: ./${script.path().join('/')}`);
            return module.parse(script.read(), source);
         } else {
            const info = module.info(source);
            if (info.installed) {
               if (info.valid) {
                  console.log(`evaluating script: ./${info.script.path().join('/')}`);
                  return module.parse(info.js, `modules/${info.source}`);
               } else {
                  return 'invalid-package';
               }
            } else {
               return 'package-unavailable';
            }
         }
      },
      source: (repo) => {
         return module.list[repo] ? repo : `${module.trusted[repo] || repo.split('/').slice(-2).join('/')}`;
      },
      trusted: {}
   };

   const index = {
      core: core,
      exports: module.exports,
      global: global,
      module: module,
      plugin: plugin,
      require: module.require,
      server: server
   };

   if (global.module) global.module.exports = index;
   else {
      core.command({
         name: 'js',
         usage: '/js <code...>',
         decription: 'Evaluates input as JavaScript code.',
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
         usage: '/module <action> [repo]',
         decription: 'Manages local and remote grakkit modules.',
         execute: (player, action, repo) => {
            if (action) {
               action = core.lc(action);
               if ([ 'add', 'create', 'remove', 'update' ].includes(action)) {
                  if (repo) {
                     if (repo === '*' && [ 'remove', 'update' ].includes(action)) {
                        let index = 0;
                        let info = core.keys(module.list);
                        if (action === 'update') info = info.filter((name) => module.list[name] !== -1);
                        const loop = () => {
                           const source = info[index];
                           ++index;
                           if (source) {
                              switch (action) {
                                 case 'remove':
                                    core.text(player, `&7module $&e ${source}&f deleting...`);
                                    try {
                                       core.folder(core.root, 'modules', source).remove(true);
                                       delete module.list[source];
                                       core.text(player, `&7module $&e ${source}&f deleted.`);
                                    } catch (error) {
                                       core.text(player, `&7module $&e ${source}&c repo folder could not be removed.`);
                                    }
                                    loop();
                                    break;
                                 case 'update':
                                    if (module.list[source] !== -1) {
                                       core.text(player, `&7module $&e ${source}&f updating...`);
                                       module
                                          .apply(source, module.list[source])
                                          .then((data) => {
                                             module.list[source] = data;
                                             core.text(player, `&7module $&e ${source}&f updated.`);
                                             loop();
                                          })
                                          .catch((error) => {
                                             core.text(player, `&7module $&e ${source}&c ${error}`);
                                             loop();
                                          });
                                    } else {
                                       loop();
                                    }
                                    break;
                              }
                           } else if (index < info.length) {
                              loop();
                           } else if (info.length === 0) {
                              core.text(player, `&7module $&c there are no modules to ${action}.`);
                           }
                        };
                        loop();
                     } else {
                        let source = null;
                        let installed = null;
                        repo = core.lc(repo).replace(/\\/g, '/');
                        switch (action) {
                           case 'add':
                              source = `${module.trusted[repo] || repo.split('/').slice(-2).join('/')}`;
                              installed = core.keys(module.list).includes(source);
                              if (installed) {
                                 core.text(player, `&7module $&e ${source}&c repository already installed.`);
                              } else {
                                 core.text(player, `&7module $&e ${source}&f installing...`);
                                 module
                                    .apply(source)
                                    .then((data) => {
                                       module.list[source] = data;
                                       core.text(player, `&7module $&e ${source}&f installed.`);
                                    })
                                    .catch((error) => {
                                       core.text(player, `&7module $&e ${source}&c ${error}`);
                                    });
                              }
                              break;
                           case 'create':
                              source = repo.replace(/.*\//g, '');
                              installed = core.keys(module.list).includes(source);
                              if (installed) {
                                 core.text(player, `&7module $&e ${source}&c repository already installed.`);
                              } else {
                                 core.text(player, `&7module $&e ${source}&f creating...`);
                                 try {
                                    const folder = core.folder(core.root, 'modules', source);
                                    folder.file('index.js').write(module.default.index);
                                    folder.file('package.json').write(module.default.package);
                                    module.list[source] = -1;
                                    core.text(player, `&7module $&e ${source}&f created.`);
                                 } catch (error) {
                                    core.text(player, `&7module $&e ${source}&c repo folder could not be created.`);
                                 }
                              }
                              break;
                           case 'remove':
                              source = module.source(repo);
                              installed = core.keys(module.list).includes(source);
                              if (installed) {
                                 core.text(player, `&7module $&e ${source}&f deleting...`);
                                 try {
                                    core.folder(core.root, 'modules', source).remove(true);
                                    delete module.list[source];
                                    core.text(player, `&7module $&e ${source}&f deleted.`);
                                 } catch (error) {
                                    core.text(player, `&7module $&e ${source}&c repo folder could not be removed.`);
                                 }
                              } else {
                                 core.text(player, `&7module $&e ${source}&c repository not already installed.`);
                              }
                              break;
                           case 'update':
                              source = module.source(repo);
                              installed = core.keys(module.list).includes(source);
                              if (installed) {
                                 if (module.list[source] === -1) {
                                    core.text(player, `&7module $&e ${source}&c cannot update a local module.`);
                                 } else {
                                    core.text(player, `&7module $&e ${source}&f updating...`);
                                    module
                                       .apply(source, module.list[source])
                                       .then((data) => {
                                          module.list[source] = data;
                                          core.text(player, `&7module $&e ${source}&f updated.`);
                                       })
                                       .catch((error) => {
                                          core.text(player, `&7module $&e ${source}&c ${error}`);
                                       });
                                 }
                              } else {
                                 core.text(player, `&7module $&e ${source}&c repository not installed.`);
                              }
                              break;
                        }
                     }
                  } else {
                     core.text(player, `&7module $&c no repository specified.`);
                  }
               } else if (action === 'list') {
                  let keys = core.keys(module.list);
                  if (keys.length === 0) {
                     core.text(player, `&7module $&c there are no modules to list.`);
                  } else {
                     core.text(player, `&7module $&f installed modules...`);
                     keys.forEach((key) => core.text(player, `&7module $&e ${key}&f [${module.list[key]}]`));
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
               return core.from(core.lc(action), [ 'add', 'create', 'remove', 'update' ]);
            } else if (extra === undefined) {
               switch (action) {
                  case 'add':
                     return core.from(core.lc(repo), core.keys(module.trusted));
                  case 'remove':
                  case 'update':
                     let info = core.keys(module.list);
                     if (action === 'update') info = info.filter((name) => module.list[name] !== -1);
                     return [ ...core.from(core.lc(repo), info), '*' ];
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

      module.fetch('https://raw.githubusercontent.com/grakkit/grakkit/master/modules.json').then((data) => {
         module.trusted = data;
      });

      Object.assign(global, index);

      const scripts = (folder) => {
         const files = folder.listFiles();
         if (files) {
            for (let index = 0; index < files.length; ++index) {
               const file = files[index];
               if (file.directory) {
                  scripts(file);
               } else {
                  const script = core.file(file.toPath().toString());
                  console.log(`evaluating script: ./${script.path().join('/')}`);
                  core.eval(script.read());
               }
            }
         }
      };

      try {
         scripts(core.folder(core.root, 'scripts').io());
      } catch (error) {
         console.error(error);
      }
   }
})();
