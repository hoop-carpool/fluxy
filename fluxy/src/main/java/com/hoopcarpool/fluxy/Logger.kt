package com.hoopcarpool.fluxy

/**
 * Interface to decouple system log from fluxy
 */
interface Logger {
    fun v(msg: String)
    fun d(msg: String)
    fun i(msg: String)
    fun w(msg: String)
    fun e(msg: String)
}

/**
 * Default [Logger] implementation
 */
class DefaultLogger : Logger {

    override fun v(msg: String) {
        println("${System.currentTimeMillis()} V/$msg")
    }

    override fun d(msg: String) {
        println("${System.currentTimeMillis()} D/$msg")
    }

    override fun i(msg: String) {
        println("${System.currentTimeMillis()} I/$msg")
    }

    override fun w(msg: String) {
        println("${System.currentTimeMillis()} W/$msg")
    }

    override fun e(msg: String) {
        println("${System.currentTimeMillis()} E/$msg")
    }
}
