package com.hoopcarpool.fluxy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class StateMerger<R> {
    val storeAndMappers = ArrayList<Pair<FluxyStore<*>, () -> R>>()

    /** Add a new store + mapper. */
    inline fun <S : FluxyStore<U>, U : Any> merge(store: S, crossinline mapper: (U.() -> R)) {
        storeAndMappers.add(store to { store.state.mapper() })
    }
}

/**
 * Builder function for [StateMerger].
 */
inline fun <R> mergeStates(hotStart: Boolean = true, crossinline builder: StateMerger<R>.() -> Unit): Flow<List<R>> {
    return StateMerger<R>().apply { builder() }.flow(hotStart)
}

/** Merge all stores + mappers into a single [Flow]. */
fun <R> StateMerger<R>.flow(hotStart: Boolean = true): Flow<List<R>> {
    return storeAndMappers
        .map { (store, fn) -> store.flow(hotStart).map { fn() } }
        .merge()
        .flowOn(Dispatchers.Main)
        .map {
            storeAndMappers.map { (_, fn) -> fn() }.toList()
        }.distinctUntilChanged()
}

/** Extension util for init a list of [FluxyStore] */
fun Iterable<FluxyStore<*>>.initAll() {
    forEach {
        val initTime = System.currentTimeMillis()
        it.init()
        it.initTime = System.currentTimeMillis() - initTime
    }
}
