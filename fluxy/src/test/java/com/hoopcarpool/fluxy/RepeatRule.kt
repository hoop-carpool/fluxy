package com.hoopcarpool.fluxy

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

public class RepeatRule : TestRule {
    private class RepeatStatement(statement: Statement, repeat: Int) : Statement() {
        private val statement: Statement
        private val repeat: Int
        @Throws(Throwable::class) override fun evaluate() {
            for (i in 0 until repeat) {
                statement.evaluate()
            }
        }

        init {
            this.statement = statement
            this.repeat = repeat
        }
    }

    override fun apply(statement: Statement?, description: Description): Statement? {
        val result: Statement?
        val repeat: Repeat = description.getAnnotation(Repeat::class.java)
        val times: Int = repeat.value
        result = statement?.let { RepeatStatement(it, times) }
        return result
    }
}

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME) @kotlin.annotation.Target(AnnotationTarget.FUNCTION)
public annotation class Repeat(val value: Int = 1)
