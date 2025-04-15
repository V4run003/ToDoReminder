package com.varun.todoreminder.util

sealed class UiEvent {
    object PopBackStack: UiEvent()
    data class Navigate(val route: String): UiEvent()
    data class PlayText(val id : Int,val title: String, val description : String?): UiEvent()
    data class StopPlaying(val id : Int,val title: String, val description : String?): UiEvent()
    data class ShowSnackbar(
        val message: String,
        val action: String? = null
    ): UiEvent()
}
