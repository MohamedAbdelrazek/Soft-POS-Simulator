package sa.com.stc.softpossimulator.model

data class IncomingExtra(
    val key: String,
    val value: String,
)

data class IncomingIntentSnapshot(
    val action: String? = null,
    val actionType: ActionType? = null,
    val mimeType: String? = null,
    val extras: List<IncomingExtra> = emptyList(),
    val extraMap: Map<String, String> = emptyMap(),
    val launchedFromLauncher: Boolean = false,
) {
    val launchSourceLabel: String
        get() = if (launchedFromLauncher) "Launcher" else "Implicit SoftPOS action"
}
