package sa.com.stc.softpossimulator.payload

data class OutgoingPayload(
    val topLevelExtras: LinkedHashMap<String, String>,
    val previewText: String,
)

data class PayloadBuildResult(
    val payload: OutgoingPayload,
    val validationErrors: List<String>,
)
