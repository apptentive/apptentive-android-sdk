package apptentive.com.android.feedback

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
object Constants {
    const val SDK_VERSION = "6.8.1"
    const val API_VERSION = 14
    const val SERVER_URL = "https://api.apptentive.com"
    const val REDACTED_DATA = "<REDACTED>"
    private const val CONVERSATION_PATH = "/conversations/:conversation_id/"

    fun buildHttpPath(path: String): String =
        CONVERSATION_PATH + path
}
