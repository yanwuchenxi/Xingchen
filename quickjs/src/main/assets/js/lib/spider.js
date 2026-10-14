import * as spider from '%s'

if (!globalThis.__JS_SPIDER__) {
    // 无论 cat 还是 export default，都挂上 req，避免 drpy request 找不到全局
    globalThis.req = http
    if (spider.__jsEvalReturn) {
        globalThis.__JS_SPIDER__ = spider.__jsEvalReturn()
    } else if (spider.default) {
        globalThis.__JS_SPIDER__ = typeof spider.default === 'function' ? spider.default() : spider.default
    }
}
