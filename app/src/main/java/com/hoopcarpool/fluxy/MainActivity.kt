package com.hoopcarpool.fluxy

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
            myStore.observe {
                Log.d("tag", "3")
                findViewById<TextView>(R.id.textView).text = it.number
            }
        }
    }
}


data class MyAction(val number: String) : BaseAction

data class MyState(val number: String = "")

class MyStore : FluxyStore<MyState>() {

    init {
        reduce<MyAction> {
            state.copy(number = it.number)
        }
    }
}