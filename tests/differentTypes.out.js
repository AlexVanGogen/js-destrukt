function foo(arr) {
  var [, a, , , , b] = arr;
  let [, , c, d, , , e] = arr;
  const [f, , g] = arr;
}
