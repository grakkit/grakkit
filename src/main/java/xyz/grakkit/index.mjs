// BEGIN BASIC
const _ = globalThis;
(_.global = _),
   (_.server = org.bukkit.Bukkit.getServer()),
   (_.plugin = _.server.getPluginManager().getPlugin('Grakkit')),
   (_.event = (a) => ({
      on: (b, c) => (a[b] || (a[b] = [])).push(c),
      fire: (b, ...c) => (a[b] || (a[b] = [])).map((a) => a(...c))
   })),
   (_.command = (a, b) => {
      b || (b = {});
      const c = b.usage,
         d = b.desc,
         e = b.error,
         f = b.aliases || [],
         g = (b.executor || (() => {})).toString(),
         h = (b.tabCompleter || (() => [])).toString(),
         i = b.prefix || 'grakkit';
      return _.plugin.command(a, c, d, e, f, g, h).register(i);
   }),
   (_.bukkit = _.event({})),
   (_.on = (event, listener) => {
      const length = _.bukkit.on(event, listener);
      return (
         1 === length &&
            _.server
               .getPluginManager()
               .registerEvent(
                  eval(event).class,
                  new (Java.extend(org.bukkit.event.Listener, {}))(),
                  org.bukkit.event.EventPriority.HIGHEST,
                  (a, b) => _.fire(event, b),
                  plugin
               ),
         length
      );
   }),
   (_.fire = (...a) => _.bukkit.fire(...a)),
   (_.tabCompleter = (a) => {
      const b = /.*(\!|\^|\&|\*|\(|\-|\+|\=|\[|\{|\||\;|\:|\,|\?|\/)/,
         c = a.replace(b, '').split('.');
      let d = global,
         e = 0;
      for (; e < c.length - 1; ) {
         let a = c[e];
         d[a] ? ((d = d[a]), ++e) : (e = 1 / 0);
      }
      if (e === c.length - 1) {
         const e = c.slice(-1)[0];
         return Object.getOwnPropertyNames(d)
            .filter((a) => a.includes(e))
            .map((d) => (a.match(b) || [ '' ])[0] + [ ...c.slice(0, -1), d ].join('.'));
      }
      return [];
   });
// END BASIC
