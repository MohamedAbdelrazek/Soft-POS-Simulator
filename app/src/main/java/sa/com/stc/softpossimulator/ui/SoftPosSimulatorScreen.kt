package sa.com.stc.softpossimulator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import sa.com.stc.softpossimulator.model.ActionType
import sa.com.stc.softpossimulator.model.FailureMode
import sa.com.stc.softpossimulator.model.ResultMode
import sa.com.stc.softpossimulator.model.SimulatorUiState
import sa.com.stc.softpossimulator.model.TransactionTypeOption
import sa.com.stc.softpossimulator.payload.SoftPosContract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftPosSimulatorScreen(
    uiState: SimulatorUiState,
    onActionSelected: (ActionType) -> Unit,
    onFormUpdated: ((sa.com.stc.softpossimulator.model.SimulatorFormData) -> sa.com.stc.softpossimulator.model.SimulatorFormData) -> Unit,
    onResetDefaults: () -> Unit,
    onReturnResult: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    val form = uiState.form

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text(
                            text = "SoftPOS Simulator",
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Chooser-compatible STC test harness",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SummaryCard(uiState)

            SectionCard(title = "Scenario") {
                DropdownField(
                    label = "SoftPOS action",
                    options = ActionType.entries,
                    selected = form.selectedAction,
                    itemLabel = { it.wireAction },
                    onSelected = onActionSelected,
                )

                Spacer(modifier = Modifier.height(12.dp))

                SelectorRow(
                    title = "Result",
                    options = ResultMode.entries,
                    selected = form.resultMode,
                    label = { it.label },
                    onSelected = { mode ->
                        onFormUpdated { it.copy(resultMode = mode) }
                    },
                )

                if (form.selectedAction.supportsFailureBranchSelector && form.resultMode == ResultMode.FAILURE) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SelectorRow(
                        title = "Failure branch",
                        options = FailureMode.entries,
                        selected = form.failureMode,
                        label = { it.label },
                        onSelected = { failureMode ->
                            onFormUpdated { it.copy(failureMode = failureMode) }
                        },
                    )
                }
            }

            IncomingIntentCard(uiState)

            SectionCard(title = "Outgoing Payload") {
                when (form.selectedAction) {
                    ActionType.REGISTRATION_STATUS -> RegistrationFields(
                        uiState = uiState,
                        onFormUpdated = onFormUpdated,
                    )

                    ActionType.PURCHASE,
                    ActionType.REFUND,
                    -> PurchaseRefundFields(
                        uiState = uiState,
                        isRefund = form.selectedAction == ActionType.REFUND,
                        onFormUpdated = onFormUpdated,
                    )

                    ActionType.LAST_TRANSACTION_DETAILS -> LastTransactionFields(
                        uiState = uiState,
                        onFormUpdated = onFormUpdated,
                    )

                    ActionType.REVERSAL -> ReversalFields(
                        uiState = uiState,
                        onFormUpdated = onFormUpdated,
                    )
                }
            }

            if (uiState.validationErrors.isNotEmpty()) {
                ValidationCard(uiState.validationErrors)
            }

            PayloadPreviewCard(
                preview = uiState.payloadPreview,
                onCopy = {
                    clipboardManager.setText(AnnotatedString(uiState.payloadPreview))
                },
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onReturnResult,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Return Result")
                }

                OutlinedButton(
                    onClick = onResetDefaults,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Reset Defaults")
                }

                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(uiState.payloadPreview))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Copy Payload Preview")
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(uiState: SimulatorUiState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Current launch context",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(uiState.incomingIntent.launchSourceLabel) },
                )
                AssistChip(
                    onClick = {},
                    label = { Text(uiState.form.selectedAction.label) },
                )
            }
            Text(
                text = "This screen can be opened manually or selected from the Android chooser for the five STC SoftPOS actions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun IncomingIntentCard(uiState: SimulatorUiState) {
    SectionCard(title = "Incoming Intent") {
        KeyValueLine(label = "Received action", value = uiState.incomingIntent.action ?: "None")
        Spacer(modifier = Modifier.height(8.dp))
        KeyValueLine(label = "Received MIME type", value = uiState.incomingIntent.mimeType ?: "None")
        Spacer(modifier = Modifier.height(8.dp))
        KeyValueLine(label = "Launch source", value = uiState.incomingIntent.launchSourceLabel)
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Extras",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.incomingIntent.extras.isEmpty()) {
            Text(
                text = "No extras received.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.incomingIntent.extras.forEach { extra ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                        ) {
                            Text(
                                text = extra.key,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            SelectionContainer {
                                Text(
                                    text = extra.value,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrationFields(
    uiState: SimulatorUiState,
    onFormUpdated: ((sa.com.stc.softpossimulator.model.SimulatorFormData) -> sa.com.stc.softpossimulator.model.SimulatorFormData) -> Unit,
) {
    val form = uiState.form
    TextFieldBlock(
        label = "terminal_id",
        value = form.terminalId,
        supportingText = "Must be at least 8 characters to avoid the caller's substring crash.",
        onValueChange = { value ->
            onFormUpdated { it.copy(terminalId = value) }
        },
    )

    if (form.resultMode == ResultMode.FAILURE) {
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "Failure code",
            value = form.registrationFailureCode,
            onValueChange = { value ->
                onFormUpdated { it.copy(registrationFailureCode = value) }
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "Failure message",
            value = form.registrationFailureMessage,
            onValueChange = { value ->
                onFormUpdated { it.copy(registrationFailureMessage = value) }
            },
        )
    }
}

@Composable
private fun PurchaseRefundFields(
    uiState: SimulatorUiState,
    isRefund: Boolean,
    onFormUpdated: ((sa.com.stc.softpossimulator.model.SimulatorFormData) -> sa.com.stc.softpossimulator.model.SimulatorFormData) -> Unit,
) {
    val form = uiState.form

    TextFieldBlock(
        label = "ORDER_ID",
        value = form.orderId,
        onValueChange = { value -> onFormUpdated { it.copy(orderId = value) } },
    )
    Spacer(modifier = Modifier.height(12.dp))
    TextFieldBlock(
        label = "formatted_amount",
        value = form.amount,
        onValueChange = { value -> onFormUpdated { it.copy(amount = value) } },
    )
    Spacer(modifier = Modifier.height(12.dp))

    if (isRefund) {
        TextFieldBlock(
            label = "Incoming ORIGINAL_TRANS_SEQ_NO",
            value = form.refundOriginalTransactionSequenceNumber,
            enabled = false,
            supportingText = "Read from the incoming request for visibility only. It is not returned in the STC result contract.",
            onValueChange = {},
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "Incoming ORIGINAL_TRANS_DATE",
            value = form.refundOriginalTransactionDate,
            enabled = false,
            onValueChange = {},
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    TextFieldBlock(
        label = "rrn",
        value = form.rrn,
        onValueChange = { value -> onFormUpdated { it.copy(rrn = value) } },
    )
    Spacer(modifier = Modifier.height(12.dp))
    TextFieldBlock(
        label = "pan",
        value = form.pan,
        onValueChange = { value -> onFormUpdated { it.copy(pan = value) } },
    )
    Spacer(modifier = Modifier.height(12.dp))
    TextFieldBlock(
        label = "terminal_id",
        value = form.terminalId,
        supportingText = "The caller slices the first 8 chars from this field.",
        onValueChange = { value -> onFormUpdated { it.copy(terminalId = value) } },
    )
    Spacer(modifier = Modifier.height(12.dp))
    TextFieldBlock(
        label = "card_name",
        value = form.cardName,
        onValueChange = { value -> onFormUpdated { it.copy(cardName = value) } },
    )
    Spacer(modifier = Modifier.height(12.dp))
    TextFieldBlock(
        label = "auth_code",
        value = form.authCode,
        onValueChange = { value -> onFormUpdated { it.copy(authCode = value) } },
    )
    Spacer(modifier = Modifier.height(12.dp))
    TextFieldBlock(
        label = "card_expiry_date",
        value = form.cardExpiryDate,
        onValueChange = { value -> onFormUpdated { it.copy(cardExpiryDate = value) } },
    )
    Spacer(modifier = Modifier.height(12.dp))
    TextFieldBlock(
        label = "transaction_request_time",
        value = form.transactionRequestTime,
        supportingText = "Expected format: yyMMddHHmmss",
        onValueChange = { value -> onFormUpdated { it.copy(transactionRequestTime = value) } },
    )
    Spacer(modifier = Modifier.height(12.dp))

    when {
        form.resultMode == ResultMode.SUCCESS -> {
            TextFieldBlock(
                label = "res_desc",
                value = form.responseDescription,
                onValueChange = { value -> onFormUpdated { it.copy(responseDescription = value) } },
            )
        }

        form.failureMode == FailureMode.DECLINED -> {
            TextFieldBlock(
                label = "resp_code",
                value = form.responseCode,
                onValueChange = { value -> onFormUpdated { it.copy(responseCode = value) } },
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextFieldBlock(
                label = "res_desc",
                value = form.responseDescription,
                onValueChange = { value -> onFormUpdated { it.copy(responseDescription = value) } },
            )
        }

        else -> {
            TextFieldBlock(
                label = "reason",
                value = form.abortReason,
                onValueChange = { value -> onFormUpdated { it.copy(abortReason = value) } },
            )
        }
    }
}

@Composable
private fun LastTransactionFields(
    uiState: SimulatorUiState,
    onFormUpdated: ((sa.com.stc.softpossimulator.model.SimulatorFormData) -> sa.com.stc.softpossimulator.model.SimulatorFormData) -> Unit,
) {
    val form = uiState.form

    TextFieldBlock(
        label = "order_id",
        value = form.orderId,
        supportingText = "STC compares this against the active order id.",
        onValueChange = { value -> onFormUpdated { it.copy(orderId = value) } },
    )
    Spacer(modifier = Modifier.height(12.dp))

    if (form.resultMode == ResultMode.SUCCESS) {
        DropdownField(
            label = "transaction_type",
            options = TransactionTypeOption.entries,
            selected = form.lastTransactionType,
            itemLabel = { it.wireValue },
            onSelected = { option ->
                onFormUpdated { it.copy(lastTransactionType = option) }
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        DropdownField(
            label = "transaction_status",
            options = listOf(
                SoftPosContract.StatusApproved,
                SoftPosContract.StatusDeclined,
                SoftPosContract.StatusFailed,
            ),
            selected = form.lastTransactionStatus,
            itemLabel = { it },
            onSelected = { value ->
                onFormUpdated { it.copy(lastTransactionStatus = value) }
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "rrn",
            value = form.rrn,
            onValueChange = { value -> onFormUpdated { it.copy(rrn = value) } },
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "pan",
            value = form.pan,
            onValueChange = { value -> onFormUpdated { it.copy(pan = value) } },
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "terminal_id",
            value = form.terminalId,
            onValueChange = { value -> onFormUpdated { it.copy(terminalId = value) } },
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "card_label_name",
            value = form.cardLabelName,
            onValueChange = { value -> onFormUpdated { it.copy(cardLabelName = value) } },
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "formatted_amount",
            value = form.amount,
            onValueChange = { value -> onFormUpdated { it.copy(amount = value) } },
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "auth_response_code",
            value = form.authResponseCode,
            onValueChange = { value -> onFormUpdated { it.copy(authResponseCode = value) } },
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "card_expiry_date",
            value = form.cardExpiryDate,
            onValueChange = { value -> onFormUpdated { it.copy(cardExpiryDate = value) } },
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "transaction_response_date",
            value = form.transactionResponseDate,
            onValueChange = { value -> onFormUpdated { it.copy(transactionResponseDate = value) } },
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "resp_code",
            value = form.lastTransactionResponseCode,
            onValueChange = { value -> onFormUpdated { it.copy(lastTransactionResponseCode = value) } },
        )
    } else {
        TextFieldBlock(
            label = "resp_code",
            value = form.lastTransactionResponseCode,
            onValueChange = { value -> onFormUpdated { it.copy(lastTransactionResponseCode = value) } },
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextFieldBlock(
            label = "message",
            value = form.lastTransactionFailureMessage,
            onValueChange = { value -> onFormUpdated { it.copy(lastTransactionFailureMessage = value) } },
        )
    }
}

@Composable
private fun ReversalFields(
    uiState: SimulatorUiState,
    onFormUpdated: ((sa.com.stc.softpossimulator.model.SimulatorFormData) -> sa.com.stc.softpossimulator.model.SimulatorFormData) -> Unit,
) {
    val form = uiState.form

    TextFieldBlock(
        label = "ORDER_ID",
        value = form.orderId,
        onValueChange = { value -> onFormUpdated { it.copy(orderId = value) } },
    )
    Spacer(modifier = Modifier.height(12.dp))

    when {
        form.resultMode == ResultMode.SUCCESS -> {
            Text(
                text = "Success returns top-level status = Approved and includes a debug result with ORDER_ID and res_desc = Reversal approved.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        form.failureMode == FailureMode.DECLINED -> {
            TextFieldBlock(
                label = "resp_code",
                value = form.responseCode,
                onValueChange = { value -> onFormUpdated { it.copy(responseCode = value) } },
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextFieldBlock(
                label = "message",
                value = form.reversalMessage,
                onValueChange = { value -> onFormUpdated { it.copy(reversalMessage = value) } },
            )
        }

        else -> {
            TextFieldBlock(
                label = "reason",
                value = form.abortReason,
                onValueChange = { value -> onFormUpdated { it.copy(abortReason = value) } },
            )
        }
    }
}

@Composable
private fun ValidationCard(errors: List<String>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Validation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            errors.forEach { error ->
                Text(
                    text = "• $error",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Composable
private fun PayloadPreviewCard(
    preview: String,
    onCopy: () -> Unit,
) {
    SectionCard(
        title = "Payload Preview",
        action = {
            TextButton(onClick = onCopy) {
                Text("Copy")
            }
        },
    ) {
        SelectionContainer {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(14.dp),
            ) {
                Text(
                    text = preview,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun TextFieldBlock(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = supportingText?.let {
            { Text(it) }
        },
        enabled = enabled,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun KeyValueLine(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(2.dp))
        SelectionContainer {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun <T> SelectorRow(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    label = { Text(label(option)) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    label: String,
    options: List<T>,
    selected: T,
    itemLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = itemLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(itemLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                action?.invoke()
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
