package sa.com.stc.softpossimulator

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import sa.com.stc.softpossimulator.payload.ResultIntentFactory
import sa.com.stc.softpossimulator.ui.SoftPosSimulatorScreen
import sa.com.stc.softpossimulator.ui.SoftPosSimulatorViewModel
import sa.com.stc.softpossimulator.ui.theme.SoftPosSimulatorTheme

class SoftPosSimulatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SoftPosSimulatorTheme {
                val context = LocalContext.current
                val viewModel: SoftPosSimulatorViewModel = viewModel()
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

                LaunchedEffect(Unit) {
                    viewModel.initializeFromIntent(intent)
                }

                SoftPosSimulatorScreen(
                    uiState = uiState,
                    onActionSelected = viewModel::selectAction,
                    onFormUpdated = viewModel::updateForm,
                    onResetDefaults = viewModel::resetDefaults,
                    onReturnResult = {
                        val payload = viewModel.buildValidatedPayload()
                        if (payload == null) {
                            Toast.makeText(
                                context,
                                "Please fix validation errors before returning the result.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            return@SoftPosSimulatorScreen
                        }

                        if (uiState.incomingIntent.launchedFromLauncher) {
                            Toast.makeText(
                                context,
                                "No caller is expected to receive this result from a launcher launch.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }

                        setResult(Activity.RESULT_OK, ResultIntentFactory.create(payload))
                        finish()
                    },
                )
            }
        }
    }
}
