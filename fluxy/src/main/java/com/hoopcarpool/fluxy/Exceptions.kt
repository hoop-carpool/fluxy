package com.hoopcarpool.fluxy

data class CyclicActionDispatchException(val action: BaseAction, val store: FluxyStore<*>) : Exception()

data class DuplicateReducerException(val clazz: String) : Exception("Reducer already exists for $clazz at this store")
