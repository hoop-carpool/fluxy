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
        println("V/$msg")
    }

    override fun d(msg: String) {
        println("D/$msg")
    }

    override fun i(msg: String) {
        println("I/$msg")
    }

    override fun w(msg: String) {
        println("W/$msg")
    }

    override fun e(msg: String) {
        println("E/$msg")
    }
}
