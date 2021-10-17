package com.example.annotationprocessing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.lang.String

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userDto = UserDto(
            id = 10,
            email = String("gabriel.samojlo@netguru.com"),
            accountBalance = 10.0
        )
    }
}
