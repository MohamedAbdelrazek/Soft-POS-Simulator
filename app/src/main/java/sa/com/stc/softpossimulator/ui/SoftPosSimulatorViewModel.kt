package sa.com.stc.softpossimulator.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import sa.com.stc.softpossimulator.model.ActionType
import sa.com.stc.softpossimulator.model.SimulatorFormData
import sa.com.stc.softpossimulator.model.SimulatorUiState
import sa.com.stc.softpossimulator.payload.OutgoingPayload
import sa.com.stc.softpossimulator.payload.PayloadFactory

class SoftPosSimulatorViewModel(
    private val payloadFactory: PayloadFactory = PayloadFactory(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(SimulatorUiState())
    val uiState: StateFlow<SimulatorUiState> = _uiState.asStateFlow()

    private var lastIncomingSnapshotInitialized = false

    fun initializeFromIntent(intent: Intent?) {
        if (lastIncomingSnapshotInitialized) return

        val snapshot = IncomingIntentParser.parse(intent)
        val action = snapshot.actionType ?: ActionType.REGISTRATION_STATUS
        lastIncomingSnapshotInitialized = true
        publish(
            form = SimulatorDefaultsFactory.create(snapshot, action),
            incomingSnapshot = snapshot,
        )
    }

    fun selectAction(action: ActionType) {
        publish(
            form = SimulatorDefaultsFactory.create(_uiState.value.incomingIntent, action),
            incomingSnapshot = _uiState.value.incomingIntent,
        )
    }

    fun resetDefaults() {
        publish(
            form = SimulatorDefaultsFactory.create(
                snapshot = _uiState.value.incomingIntent,
                action = _uiState.value.form.selectedAction,
            ),
            incomingSnapshot = _uiState.value.incomingIntent,
        )
    }

    fun updateForm(transform: (SimulatorFormData) -> SimulatorFormData) {
        publish(
            form = transform(_uiState.value.form),
            incomingSnapshot = _uiState.value.incomingIntent,
        )
    }

    fun buildValidatedPayload(): OutgoingPayload? {
        val current = _uiState.value
        val result = payloadFactory.build(current.form)
        _uiState.value = current.copy(
            payloadPreview = result.payload.previewText,
            validationErrors = result.validationErrors,
        )
        return result.payload.takeIf { result.validationErrors.isEmpty() }
    }

    private fun publish(
        form: SimulatorFormData,
        incomingSnapshot: sa.com.stc.softpossimulator.model.IncomingIntentSnapshot,
    ) {
        val result = payloadFactory.build(form)
        _uiState.value = SimulatorUiState(
            form = form,
            incomingIntent = incomingSnapshot,
            payloadPreview = result.payload.previewText,
            validationErrors = result.validationErrors,
        )
    }
}
