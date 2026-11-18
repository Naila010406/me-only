package com.example.tiketiin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Pembuat ViewModel yang butuh repository, pengganti companion object factory
// di tiap ViewModel pada tugas navigation (isinya sama, hanya ditulis sekali).
fun <VM : ViewModel> simpleFactory(builder: () -> VM): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return builder() as T
        }
    }
}
