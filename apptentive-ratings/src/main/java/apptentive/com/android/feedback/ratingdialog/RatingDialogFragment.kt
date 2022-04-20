package apptentive.com.android.feedback.ratingdialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import apptentive.com.android.feedback.ratings.R
import apptentive.com.android.ui.overrideTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView

internal class RatingDialogFragment : DialogFragment() {

    private val viewModel by viewModels<RatingDialogViewModel>()

    @SuppressLint("UseGetLayoutInflater", "InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {

            val contextWrapper = ContextThemeWrapper(requireContext(), R.style.Theme_Apptentive).apply {
                overrideTheme()
            }
            val inflater = LayoutInflater.from(contextWrapper)
            val contentView = inflater.inflate(R.layout.apptentive_rating_dialog, null)
            setView(contentView)

            val titleView = contentView.findViewById<MaterialTextView>(R.id.apptentive_rating_dialog_title)
            titleView.text = viewModel.title.orEmpty()

            val messageView = contentView.findViewById<MaterialTextView>(R.id.apptentive_rating_dialog_message)
            messageView.text = viewModel.message.orEmpty()

            val rateButton = contentView.findViewById<MaterialButton>(R.id.apptentive_rating_dialog_button)
            rateButton.text = viewModel.rateText.orEmpty()
            rateButton.setOnClickListener {
                viewModel.onRateButton()
                dismiss()
            }

            val remindButton = contentView.findViewById<MaterialButton>(R.id.apptentive_rating_dialog_remind_button)
            remindButton.text = viewModel.remindText.orEmpty()
            remindButton.setOnClickListener {
                viewModel.onRemindButton()
                dismiss()
            }

            val declineButton = contentView.findViewById<MaterialButton>(R.id.apptentive_rating_dialog_decline_button)
            declineButton.text = viewModel.declineText.orEmpty()
            declineButton.setOnClickListener {
                viewModel.onDeclineButton()
                dismiss()
            }
        }.create()
        return dialog.apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.onCancel()
        super.onCancel(dialog)
    }
}
