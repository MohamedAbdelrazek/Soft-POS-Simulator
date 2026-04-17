package sa.com.stc.softpossimulator.payload

import android.content.Intent

object ResultIntentFactory {
    fun create(payload: OutgoingPayload): Intent =
        Intent().apply {
            payload.topLevelExtras.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
}
