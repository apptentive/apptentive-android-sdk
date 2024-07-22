package apptentive.com.android.feedback.ratingdialog

import androidx.lifecycle.ViewModel
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.LiveEvent
import apptentive.com.android.core.MissingProviderException
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.utils.getInteractionBackup
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

internal class RatingDialogViewModel : ViewModel() {
    val dismissInteraction = LiveEvent<Unit>()
    private val context: EngagementContext? = try {
        DependencyProvider.of<EngagementContextFactory>().engagementContext()
    } catch (exception: MissingProviderException) {
        dismissInteraction.postValue(Unit)
        Log.e(
            INTERACTIONS,
            "EngagementContextFactory is not registered, cannot launch RatingDialogViewModel",
            exception
        )
        null
    }
    private val interaction: RatingDialogInteraction = try {
        DependencyProvider.of<RatingDialogInteractionFactory>().getRatingDialogInteraction()
    } catch (exception: Exception) {
        getInteractionBackup()
    }

    val title = interaction.title
    val message = interaction.body
    val rateText = interaction.rateText
    val remindText = interaction.remindText
    val declineText = interaction.declineText

    fun onRateButton() {
        Log.i(INTERACTIONS, "Rating Dialog rate button pressed")
        engageCodePoint(CODE_POINT_RATE)
    }
    fun onRemindButton() {
        Log.i(INTERACTIONS, "Rating Dialog remind button pressed")
        engageCodePoint(CODE_POINT_REMIND)
    }
    fun onDeclineButton() {
        Log.i(INTERACTIONS, "Rating Dialog decline button pressed")
        engageCodePoint(CODE_POINT_DECLINE)
    }
    fun onCancel() {
        Log.i(INTERACTIONS, "Rating Dialog cancelled")
        engageCodePoint(CODE_POINT_CANCEL)
    }

    private fun engageCodePoint(name: String) {
        context?.executors?.state?.execute {
            context.engage(
                event = Event.internal(name, interaction.type),
                interactionId = interaction.id
            )
        }
    }

    companion object {
        const val CODE_POINT_RATE = "rate"
        const val CODE_POINT_REMIND = "remind"
        const val CODE_POINT_DECLINE = "decline"
        const val CODE_POINT_DISMISS = "dismiss"
        const val CODE_POINT_CANCEL = "cancel"
    }
}
