function foo(arr) {
    var a = arr[0], b = arr[1];
    a = arr[2];
    b = arr[3];
    [a, b] = [b, a];
}