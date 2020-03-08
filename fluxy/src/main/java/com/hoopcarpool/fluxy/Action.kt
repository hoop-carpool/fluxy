package com.hoopcarpool.fluxy

/**
 * Base class needed to be dispatched though the [Dispatcher]
 */
interface BaseAction

/**
 * Base class needed to be dispatched though the [Dispatcher]'s [Dispatcher.dispatchAsync]
 */
@FluxyPreview
interface AsyncAction : BaseAction
