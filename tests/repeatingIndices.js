function bar(arr) {
    const a = arr[0], b = arr[1];
    const c = arr[1];       // Will not be converted
    var bad = 42;
    const d = arr[3];
}