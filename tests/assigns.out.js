function foo(arr) {
  var [a, b] = arr;
  [, , a, b] = arr;
  [a, b] = [b, a];
}
