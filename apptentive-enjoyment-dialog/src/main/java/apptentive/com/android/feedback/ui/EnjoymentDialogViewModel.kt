package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.Interaction

data class EnjoymentDialogViewModel(
    private val context: EngagementContext,
    private val interaction: Interaction
) {
    private val stateExecutor = context.executors.state

    fun onYesButton() {
        engageCodePoint(CODE_POINT_YES)
    }

    fun onNoButton() {
        engageCodePoint(CODE_POINT_NO)
    }

    fun onDismissButton() {
        engageCodePoint(CODE_POINT_DISMISS)
    }

    fun onCancel() {
        engageCodePoint(CODE_POINT_CANCEL)
    }

    private fun engageCodePoint(name: String) {
        stateExecutor.execute {
            context.engage(
                event = Event.internal(name, interaction.type),
                interactionId = interaction.id
            )
        }
    }

    companion object {
        internal const val CODE_POINT_DISMISS = "dismiss"
        internal const val CODE_POINT_CANCEL = "cancel"
        internal const val CODE_POINT_YES = "yes"
        internal const val CODE_POINT_NO = "no"
    }
}