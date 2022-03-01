package apptentive.com.android.feedback.model

import apptentive.com.android.core.BuildConfig
import kotlin.random.Random

data class RandomSampling(
    val percents: MutableMap<String, Double> = mutableMapOf(),
) {
    fun getOrPutRandomValue(id: String, debug: Boolean = BuildConfig.DEBUG): Double {
        return percents.getOrPut(id) {
            if (debug) 50.0 else Random.nextDouble(99.9)
        }
    }

    fun getRandomValue(): Double {
        return Random.nextDouble(99.9)
    }
}
