package apptentive.com.app

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)

        val isNightMode = prefs.getBoolean(EXTRA_NIGHT_MODE, false)
        delegate.localNightMode = if (isNightMode) MODE_NIGHT_YES else MODE_NIGHT_NO

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nightSwitch.isChecked = isNightMode
        binding.nightSwitch.setOnCheckedChangeListener { _, isChecked ->
            delegate.localNightMode = if (isChecked) MODE_NIGHT_YES else MODE_NIGHT_NO
            prefs.edit().putBoolean(EXTRA_NIGHT_MODE, isChecked).apply()
        }

        val shouldSanitize = prefs.getBoolean(SHOULD_SANITIZE, false)
        binding.sanitizeSwitch.isChecked = shouldSanitize
        binding.sanitizeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(SHOULD_SANITIZE, isChecked).apply()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask()
        }

        binding.infoIcon.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        binding.dataButton.setOnClickListener {
            val intent = Intent(this, DataActivity::class.java)
            startActivity(intent)
        }

        binding.engageEventButton.setOnClickListener {
            val engageEvent = binding.eventTextEditText.text?.toString()?.trim()
            if (!engageEvent.isNullOrEmpty()) {
                Apptentive.engage(this, engageEvent) { handleResult(it) }
                binding.eventTextLayout.isErrorEnabled = false
                binding.eventTextLayout.error = ""
                binding.eventTextEditText.setText("")
            } else {
                binding.eventTextLayout.isErrorEnabled = true
                binding.eventTextLayout.error = "No event entered"
            }
        }

        binding.loveDialogButton.setOnClickListener {
            Apptentive.engage(this, "love_dialog_event") { handleResult(it) }
        }

        binding.surveyButton.setOnClickListener {
            Apptentive.engage(this, "survey_event") { handleResult(it) }
        }

        binding.noteButton.setOnClickListener {
            Apptentive.engage(this, "note_event") { handleResult(it) }
        }

        binding.ratingDialogButton.setOnClickListener {
            Apptentive.engage(this, "rating_dialog_event") { handleResult(it) }
        }

        binding.clearAppDataButton.setOnClickListener {
            (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
        }
    }

    private fun handleResult(it: EngagementResult) {
        if (it !is EngagementResult.Success) {
            Toast.makeText(this, "Not engaged: $it", Toast.LENGTH_LONG).show()
        }
    }
}
