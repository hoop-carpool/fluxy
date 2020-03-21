package com.hoopcarpool.timberlogger

import com.hoopcarpool.fluxy.Logger
import timber.log.Timber

class TimberLogger : Logger {
    override fun d(msg: String) = Timber.tag("Fluxy").d(msg)

    override fun e(msg: String) = Timber.tag("Fluxy").e(msg)

    override fun i(msg: String) = Timber.tag("Fluxy").i(msg)

    override fun v(msg: String) = Timber.tag("Fluxy").v(msg)

    override fun w(msg: String) = Timber.tag("Fluxy").w(msg)
}