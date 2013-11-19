# Angular SockJS IM Demo

**DEPRECATED** in favour of [angular-sockjs-clojure](https://github.com/chlorinejs-demos/angular-sockjs-clojure)

A simple instant messaging app written in Clojure/[Chlorine](https://github.com/chlorinejs/chlorine)

You need Java, NodeJS and Bower installed.

## Installation
Grab the dependencies:

```
npm install && bower install
```

## Compilation

Type:
```
npm run-script compile
```

to have all Chlorine `*.cl2` files compiled to javascript.

or you can also type instead:
```
npm run-script watch
```

so that the compiler will re-compile all everytime there's a change to the source files.

## Running it

Run the nodejs app like normal:
```
node app.js
```

And navigate to `localhost:3000`

## Online demo

See it online here:

http://sockjs.ap01.aws.af.cm/
