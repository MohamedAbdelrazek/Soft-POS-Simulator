package sa.com.stc.softpossimulator.model

data class SimulatorUiState(
    val form: SimulatorFormData = SimulatorFormData(),
    val incomingIntent: IncomingIntentSnapshot = IncomingIntentSnapshot(),
    val payloadPreview: String = "",
    val validationErrors: List<String> = emptyList(),
)
