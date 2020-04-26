// BEGIN BASIC
const _ = globalThis;

_._ = _;
_.server = org.bukkit.Bukkit.getServer();
_.plugin = _.server.getPluginManager().getPlugin('Grakkit');

_.event = (store) => {
   return {
      on: (event, listener) => {
         return (store[event] || (store[event] = [])).push(listener);
      },
      fire: (event, ...data) => {
         return (store[event] || (store[event] = [])).map((listener) => listener(...data));
      }
   };
};

_.command = (name, options) => {
   options || (options = {});
   const usage = options.usage;
   const desc = options.desc;
   const error = options.error;
   const aliases = options.aliases || [];
   const executor = (options.executor || (() => {})).toString();
   const tabCompleter = (options.tabCompleter || (() => [])).toString();
   const prefix = options.prefix || 'grakkit';
   return _.plugin.command(name, usage, desc, error, aliases, executor, tabCompleter).register(prefix);
};

_.bukkit = _.event({});
_.on = (event, listener) => {
   const length = _.bukkit.on(event, listener);
   if (length === 1) {
      _.server
         .getPluginManager()
         .registerEvent(
            eval(event).class,
            new (Java.extend(org.bukkit.event.Listener, {}))(),
            org.bukkit.event.EventPriority.HIGHEST,
            (info, data) => _.fire(event, data),
            plugin
         );
   }
   return length;
};
_.fire = (...args) => _.bukkit.fire(...args);

_.tabCompleter = (input) => {
   const filter = /.*(\!|\^|\&|\*|\(|\-|\+|\=|\[|\{|\||\;|\:|\,|\?|\/)/;
   const nodes = input.replace(filter, '').split('.');
   let context = _;
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
      return Object.getOwnPropertyNames(context)
         .filter((key) => key.includes(segment))
         .map((comp) => (input.match(filter) || [ '' ])[0] + [ ...nodes.slice(0, -1), comp ].join('.'));
   } else {
      return [];
   }
};
// END BASIC
