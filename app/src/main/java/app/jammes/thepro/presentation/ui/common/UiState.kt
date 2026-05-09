package app.jammes.thepro.presentation.ui.common

sealed interface UiMessage {
    data class Info(val text: String) : UiMessage
    data class Error(val text: String) : UiMessage
}
