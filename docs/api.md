# Plugin API

## Config

Grakkit supports different configuration file flavors, such as:

* .grakkitrc
* config.yml
* grakkit.json
* package.json

Example `config.yml`

```
main: myEntryPoint.js
```

`main` will allow you to change the entry point for Grakkit.

## Register onDisable

Normally with plugins, you can customize your own onEnable and onDisable to meet your needs. With Grakkit, you can register your own callback for onDisable.

You can do something like the following:

```javascript
core.plugin.registerOnDisable(() => {
  console.log('Detected Grakkit Shutdown')
  foo()
  bar()
})
```