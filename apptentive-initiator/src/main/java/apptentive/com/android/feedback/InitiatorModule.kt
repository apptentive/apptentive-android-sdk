package apptentive.com.android.feedback

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.initiator.interaction.InitiatorInteraction
import apptentive.com.android.feedback.initiator.interaction.InitiatorInteractionLauncher
import apptentive.com.android.feedback.initiator.interaction.InitiatorInteractionTypeConverter

@Keep
internal class InitiatorModule : InteractionModule<InitiatorInteraction> {
    override val interactionClass = InitiatorInteraction::class.java

    override fun provideInteractionTypeConverter(): InteractionTypeConverter<InitiatorInteraction> {
        return InitiatorInteractionTypeConverter()
    }

    override fun provideInteractionLauncher(): InteractionLauncher<InitiatorInteraction> {
        return InitiatorInteractionLauncher()
    }
}
