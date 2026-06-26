package com.example.calc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.calc.ui.screen.CalculatorView
import com.example.calc.ui.screen.CalculatorViewModel
import com.example.calc.ui.theme.CalcTheme

class MainActivity : ComponentActivity() {
    val calculatorViewModel by viewModels<CalculatorViewModel> {
        viewModelFactory {
            initializer {
                val calcApplication = application as CalcApplication
                CalculatorViewModel(
                    calcApplication.ratesRepository,
                    calcApplication.appPreferenceStore
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            CalcTheme {
                CalculatorView(calculatorViewModel)
            }
        }
    }
}