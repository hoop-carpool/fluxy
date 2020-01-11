package com.hoopcarpool.fluxy

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dispatcher = Dispatcher()
        val myStore = MyStore()
        dispatcher.stores = listOf(myStore)

        findViewById<View>(R.id.button).setOnClickListener {
            dispatcher.dispatch(MyAction("${Random.nextInt(100000)}"))
        }

        GlobalScope.launch {
            myStore.flow().collect {
                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.textView).text = it.number
                }
            }
        }
    }
}

data class MyState(
    val number: String = ""
)

data class MyAction(val number: String) : BaseAction

class MyStore : FluxyStore<MyState>() {

    init {
        subscribe<MyAction> {
            state.copy(number = it.number)
        }
    }
}