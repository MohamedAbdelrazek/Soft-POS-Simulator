package sa.com.stc.softpossimulator.ui

import android.content.Intent
import android.os.Bundle
import sa.com.stc.softpossimulator.model.ActionType
import sa.com.stc.softpossimulator.model.IncomingExtra
import sa.com.stc.softpossimulator.model.IncomingIntentSnapshot

object IncomingIntentParser {
    fun parse(intent: Intent?): IncomingIntentSnapshot {
        val extraMap = bundleToMap(intent?.extras)
        val extras = extraMap.entries.map { (key, value) ->
            IncomingExtra(key = key, value = value)
        }

        return IncomingIntentSnapshot(
            action = intent?.action,
            actionType = ActionType.fromAction(intent?.action),
            mimeType = intent?.type,
            extras = extras,
            extraMap = extraMap,
            launchedFromLauncher = intent?.action == Intent.ACTION_MAIN || intent?.action == null,
        )
    }

    private fun bundleToMap(bundle: Bundle?): Map<String, String> {
        if (bundle == null) return emptyMap()

        return bundle.keySet()
            .sorted()
            .associateWith { key ->
                anyToDisplayString(bundle.get(key))
            }
    }

    private fun anyToDisplayString(value: Any?): String =
        when (value) {
            null -> "null"
            is Array<*> -> value.joinToString(prefix = "[", postfix = "]") { anyToDisplayString(it) }
            is IntArray -> value.joinToString(prefix = "[", postfix = "]")
            is LongArray -> value.joinToString(prefix = "[", postfix = "]")
            is FloatArray -> value.joinToString(prefix = "[", postfix = "]")
            is DoubleArray -> value.joinToString(prefix = "[", postfix = "]")
            is BooleanArray -> value.joinToString(prefix = "[", postfix = "]")
            is ShortArray -> value.joinToString(prefix = "[", postfix = "]")
            is ByteArray -> value.joinToString(prefix = "[", postfix = "]")
            is CharArray -> value.concatToString()
            else -> value.toString()
        }
}
