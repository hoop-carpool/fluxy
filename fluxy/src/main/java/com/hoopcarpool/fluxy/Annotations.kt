package com.hoopcarpool.fluxy

import kotlin.RequiresOptIn.Level

@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(level = Level.WARNING)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS, AnnotationTarget.PROPERTY)
annotation class FluxyPreview

@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(level = Level.ERROR)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS, AnnotationTarget.PROPERTY)
annotation class FluxyPreviewCritical
