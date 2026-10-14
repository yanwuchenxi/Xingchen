let req = (url, options) => http(url, Object.assign({
    async: false
}, options || {}));

function http(url, options) {
    options = options || {};
    // 同步：直接走 _http，避免单线程 executor 上 Promise+complete 死锁
    if (options.async === false) return _http(url, options);
    return new Promise(function (resolve) {
        _http(url, Object.assign({
            complete: function (res) { resolve(res); }
        }, options));
    }).catch(function (err) {
        console.error(err && err.name, err && err.message, err && err.stack);
        return { ok: false, status: 500, url: url };
    });
}
