package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.util.getList
import apptentive.com.android.util.getMap
import apptentive.com.android.util.getString
import apptentive.com.android.util.optString

class TextModalInteractionTypeConverter : InteractionTypeConverter<TextModalInteraction> {
    @Suppress("UNCHECKED_CAST")
    override fun convert(data: InteractionData) = TextModalInteraction(
        id = data.id,
        title = data.configuration.optString("title"),
        body = data.configuration.optString("body"),
        actions = (data.configuration.getList("actions") as List<Map<String, Any?>>).map(::convertAction)
    )

    private fun convertAction(data: Map<String, Any?>): TextModalInteraction.Action {
        val id = data.getString("id")
        val label = data.getString("label")
        val action = data.getString("action")
        @Suppress("UNCHECKED_CAST")
        return when (action) {
            "interaction" -> TextModalInteraction.Action.Invoke(
                id = id,
                label = label,
                invocations = (data.getList("invokes") as List<Map<String, Any?>>).map(::convertInvocation)
            )
            "dismiss" -> TextModalInteraction.Action.Dismiss(
                id = id,
                label = label
            )
            else -> throw IllegalArgumentException("Unexpected action: $action")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun convertInvocation(data: Map<String, Any?>) = InvocationData(
        interactionId = data.getString("interaction_id"),
        criteria = data.getMap("criteria") as Map<String, Any>
    )
}
