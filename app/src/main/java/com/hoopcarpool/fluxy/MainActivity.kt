package com.hoopcarpool.fluxy

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dispatcher = Dispatcher()
        val controller = MyController(dispatcher)
        val myStore = MyStore(controller)
        dispatcher.stores = listOf(myStore)

        findViewById<View>(R.id.button).setOnClickListener {
            dispatcher.dispatch(MyAsyncAction("${Random.nextInt(100000)}"))
        }

        GlobalScope.launch {
            myStore.observe {
                findViewById<TextView>(R.id.textView).text = it.number
                findViewById<TextView>(R.id.textView).text =
                    when (val number = it.asyncNumber) {
                        is Result.Success -> number.value
                        is Result.Loading -> "Loading"
                        is Result.Failure -> "Failure"
                        is Result.Empty -> "Empty"
                    }
            }
        }
    }
}

data class MyAction(val number: String) : BaseAction
data class MyAsyncAction(val number: String) : BaseAction
data class MyAsyncResultAction(val number: String) : BaseAction

data class MyState(
    val number: String = "",
    val asyncNumber: Result<String> = Result.Empty()
)

class MyStore(val controller: MyController) : FluxyStore<MyState>() {

    init {
        reduce<MyAction> {
            state.copy(number = it.number)
        }

        reduce<MyAsyncAction> {
            controller.delay(it.number)
            state.copy(asyncNumber = Result.Loading())
        }

        reduce<MyAsyncResultAction> {
            state.copy(asyncNumber = Result.Success(it.number))
        }
    }
}

class MyController(val dispatcher: Dispatcher) {
    fun delay(number: String) {
        GlobalScope.launch {
            delay(1000)
            dispatcher.dispatch(MyAsyncResultAction(number))
        }
    }
}