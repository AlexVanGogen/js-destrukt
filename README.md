# js-destrukt

Simple tool that finds statements in JavaScript which can be combined into [destructuring assignment](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Destructuring_assignment).

## Build & run

#### Alternative 1

Build:

```
./gradlew build
```

Run:

```
./destrukt-gradle <filename.js>
```

#### Alternative 2

Build:

```
./gradlew installDist
```

Run:

```
./destrukt <filename.js>
```

## Description

Program takes single JavaScript file as an input, processes it in the following way:
* builds AST;
* traverses it and collects statements that can be useful from the point of combining;
* analyzes statements and produces some data about which nodes can be combined and into what they can be combined;
* replaces these nodes with new, generated nodes;
* converts modified AST back to js code and writes it to the file with extension "out.js".

For parsing source code, AST traversing and manipulation, [Closure Compiler](https://developers.google.com/closure/compiler/) features are used.

## Examples

```javascript
// tests/vars.js (input)
function foo(arr) {
    var a = arr[0];
    var b = arr[1];
}

function bar(arr) {
    var a = arr[0], b = arr[1];
    var c = arr[2];
    var bad = 42;
    var d = arr[3];
}
```

```
# Run
./destrukt tests/vars.js
File tests/vars.out.js created successfully
```

```javascript
// tests/vars.out.js (output)
function foo(arr) {
  var [a, b] = arr;
}
function bar(arr) {
  var [a, b, c, d] = arr;
  var bad = 42;
}
```

Other examples can be found in [tests](https://github.com/AlexVanGogen/js-destrukt/tree/master/tests) directory.
