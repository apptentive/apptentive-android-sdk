package apptentive.com.android.feedback.survey.viewmodel

import android.text.method.LinkMovementMethod
import android.view.View
import apptentive.com.android.feedback.survey.R
import apptentive.com.android.feedback.utils.HtmlWrapper.linkifiedHTMLString
import apptentive.com.android.ui.ApptentiveViewHolder
import apptentive.com.android.ui.ListViewItem
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.SURVEY
import com.google.android.material.textview.MaterialTextView

internal class SurveyHeaderListItem(val instructions: String) : SurveyListItem(
    id = "header",
    type = Type.Header
) {
    override fun getChangePayloadMask(oldItem: ListViewItem): Int {
        return 0 // this item never changes dynamically
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SurveyHeaderListItem) return false
        if (!super.equals(other)) return false

        if (instructions != other.instructions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + instructions.hashCode()
        return result
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(instructions=$instructions)"
    }

    class ViewHolder(itemView: View) : ApptentiveViewHolder<SurveyHeaderListItem>(itemView) {
        private val introductionView = itemView.findViewById<MaterialTextView>(R.id.apptentive_survey_introduction)

        override fun bindView(item: SurveyHeaderListItem, position: Int) {
            introductionView.text = linkifiedHTMLString(item.instructions)
            try {
                introductionView.movementMethod = LinkMovementMethod.getInstance()
            } catch (exception: Exception) {
                Log.e(SURVEY, "Couldn't add linkify to survey introduction text", exception)
            }
        }
    }
}
