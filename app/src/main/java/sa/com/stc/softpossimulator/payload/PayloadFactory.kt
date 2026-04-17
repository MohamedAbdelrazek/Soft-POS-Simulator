package sa.com.stc.softpossimulator.payload

import sa.com.stc.softpossimulator.model.ActionType
import sa.com.stc.softpossimulator.model.FailureMode
import sa.com.stc.softpossimulator.model.ResultMode
import sa.com.stc.softpossimulator.model.SimulatorFormData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class PayloadFactory(
    private val json: Json = Json { prettyPrint = true },
) {
    fun build(form: SimulatorFormData): PayloadBuildResult {
        val extras = when (form.selectedAction) {
            ActionType.REGISTRATION_STATUS -> buildRegistrationPayload(form)
            ActionType.PURCHASE -> buildPurchasePayload(form)
            ActionType.REFUND -> buildRefundPayload(form)
            ActionType.LAST_TRANSACTION_DETAILS -> buildLastTransactionPayload(form)
            ActionType.REVERSAL -> buildReversalPayload(form)
        }

        return PayloadBuildResult(
            payload = OutgoingPayload(
                topLevelExtras = extras,
                previewText = buildPreview(extras),
            ),
            validationErrors = validate(form),
        )
    }

    private fun validate(form: SimulatorFormData): List<String> =
        buildList {
            if (requiresTerminalId(form) && form.terminalId.length < 8) {
                add("terminal_id must be at least 8 characters long.")
            }

            if (requiresOrderId(form) && form.orderId.isBlank()) {
                add("ORDER_ID cannot be blank for ${form.selectedAction.label}.")
            }

            if (requiresTransactionRequestTime(form) && !form.transactionRequestTime.matches(TWELVE_DIGIT_TIMESTAMP)) {
                add("transaction_request_time must be a 12-digit yyMMddHHmmss value.")
            }

            if (form.selectedAction == ActionType.LAST_TRANSACTION_DETAILS && form.resultMode == ResultMode.SUCCESS) {
                if (form.lastTransactionStatus != SoftPosContract.StatusApproved) {
                    add("LASTTRANSACTIONDETAILS success must use transaction_status = Approved.")
                }
            }
        }

    private fun requiresTerminalId(form: SimulatorFormData): Boolean =
        form.resultMode == ResultMode.SUCCESS && when (form.selectedAction) {
            ActionType.REGISTRATION_STATUS,
            ActionType.PURCHASE,
            ActionType.REFUND,
            ActionType.LAST_TRANSACTION_DETAILS
            -> true

            ActionType.REVERSAL -> false
        }

    private fun requiresOrderId(form: SimulatorFormData): Boolean =
        when (form.selectedAction) {
            ActionType.PURCHASE,
            ActionType.REFUND,
            ActionType.REVERSAL
            -> true

            ActionType.LAST_TRANSACTION_DETAILS -> form.resultMode == ResultMode.SUCCESS
            ActionType.REGISTRATION_STATUS -> false
        }

    private fun requiresTransactionRequestTime(form: SimulatorFormData): Boolean =
        form.resultMode == ResultMode.SUCCESS &&
            (form.selectedAction == ActionType.PURCHASE || form.selectedAction == ActionType.REFUND)

    private fun buildRegistrationPayload(form: SimulatorFormData): LinkedHashMap<String, String> =
        if (form.resultMode == ResultMode.SUCCESS) {
            linkedMapOf(
                SoftPosContract.ExtraStatus to SoftPosContract.StatusSuccess,
                SoftPosContract.ExtraData to buildJsonObject {
                    put("terminal_id", JsonPrimitive(form.terminalId))
                }.toString(),
            )
        } else {
            linkedMapOf(
                SoftPosContract.ExtraStatus to SoftPosContract.StatusFailed,
                SoftPosContract.ExtraResult to buildJsonObject {
                    put("code", JsonPrimitive(form.registrationFailureCode.toIntOrNull() ?: 404))
                    put("message", JsonPrimitive(form.registrationFailureMessage))
                }.toString(),
            )
        }

    private fun buildPurchasePayload(form: SimulatorFormData): LinkedHashMap<String, String> =
        buildPaymentPayload(form)

    private fun buildRefundPayload(form: SimulatorFormData): LinkedHashMap<String, String> =
        buildPaymentPayload(form)

    private fun buildPaymentPayload(form: SimulatorFormData): LinkedHashMap<String, String> =
        when {
            form.resultMode == ResultMode.SUCCESS -> linkedMapOf(
                SoftPosContract.ExtraStatus to SoftPosContract.StatusApproved,
                SoftPosContract.ExtraResult to buildJsonObject {
                    put("ORDER_ID", JsonPrimitive(form.orderId))
                    put("rrn", JsonPrimitive(form.rrn))
                    put("pan", JsonPrimitive(form.pan))
                    put("terminal_id", JsonPrimitive(form.terminalId))
                    put("card_name", JsonPrimitive(form.cardName))
                    put("formatted_amount", JsonPrimitive(form.amount))
                    put("auth_code", JsonPrimitive(form.authCode))
                    put("card_expiry_date", JsonPrimitive(form.cardExpiryDate))
                    put("transaction_request_time", JsonPrimitive(form.transactionRequestTime))
                    put("res_desc", JsonPrimitive(form.responseDescription))
                }.toString(),
            )

            form.failureMode == FailureMode.DECLINED -> linkedMapOf(
                SoftPosContract.ExtraStatus to SoftPosContract.StatusDeclined,
                SoftPosContract.ExtraResult to buildJsonObject {
                    put("resp_code", JsonPrimitive(form.responseCode))
                    put("res_desc", JsonPrimitive(form.responseDescription))
                }.toString(),
            )

            else -> linkedMapOf(
                SoftPosContract.ExtraStatus to SoftPosContract.StatusAborted,
                SoftPosContract.ExtraReason to form.abortReason,
            )
        }

    private fun buildLastTransactionPayload(form: SimulatorFormData): LinkedHashMap<String, String> =
        if (form.resultMode == ResultMode.SUCCESS) {
            val messageJson = buildJsonObject {
                put("transaction_status", JsonPrimitive(form.lastTransactionStatus))
                put("order_id", JsonPrimitive(form.orderId))
                put("transaction_type", JsonPrimitive(form.lastTransactionType.wireValue))
                put("rrn", JsonPrimitive(form.rrn))
                put("pan", JsonPrimitive(form.pan))
                put("terminal_id", JsonPrimitive(form.terminalId))
                put("card_label_name", JsonPrimitive(form.cardLabelName))
                put("formatted_amount", JsonPrimitive(form.amount))
                put("auth_response_code", JsonPrimitive(form.authResponseCode))
                put("card_expiry_date", JsonPrimitive(form.cardExpiryDate))
                put("transaction_response_date", JsonPrimitive(form.transactionResponseDate))
            }

            linkedMapOf(
                SoftPosContract.ExtraStatus to SoftPosContract.StatusSuccess,
                SoftPosContract.ExtraResult to buildJsonObject {
                    put("message", JsonPrimitive(messageJson.toString()))
                    put("resp_code", JsonPrimitive(form.lastTransactionResponseCode))
                }.toString(),
            )
        } else {
            linkedMapOf(
                SoftPosContract.ExtraStatus to SoftPosContract.StatusFailed,
                SoftPosContract.ExtraResult to buildJsonObject {
                    put("message", JsonPrimitive(form.lastTransactionFailureMessage))
                    put("resp_code", JsonPrimitive(form.lastTransactionResponseCode))
                }.toString(),
            )
        }

    private fun buildReversalPayload(form: SimulatorFormData): LinkedHashMap<String, String> =
        when {
            form.resultMode == ResultMode.SUCCESS -> linkedMapOf(
                SoftPosContract.ExtraStatus to SoftPosContract.StatusApproved,
                SoftPosContract.ExtraResult to buildJsonObject {
                    put("ORDER_ID", JsonPrimitive(form.orderId))
                    put("res_desc", JsonPrimitive("Reversal approved"))
                }.toString(),
            )

            form.failureMode == FailureMode.DECLINED -> linkedMapOf(
                SoftPosContract.ExtraStatus to SoftPosContract.StatusDeclined,
                SoftPosContract.ExtraResult to buildJsonObject {
                    put("resp_code", JsonPrimitive(form.responseCode))
                    put("message", JsonPrimitive(form.reversalMessage))
                }.toString(),
            )

            else -> linkedMapOf(
                SoftPosContract.ExtraStatus to SoftPosContract.StatusAborted,
                SoftPosContract.ExtraReason to form.abortReason,
            )
        }

    private fun buildPreview(extras: LinkedHashMap<String, String>): String =
        buildString {
            extras.entries.forEachIndexed { index, entry ->
                val key = entry.key
                val value = entry.value
                append(key)
                append(" =")
                appendLine()
                appendLine(renderPreviewValue(key, value))
                if (index != extras.size - 1) {
                    appendLine()
                }
            }
        }.trim()

    private fun renderPreviewValue(key: String, value: String): String {
        if (key != SoftPosContract.ExtraData && key != SoftPosContract.ExtraResult) {
            return value
        }

        return runCatching {
            val parsed = json.parseToJsonElement(value)
            json.encodeToString(JsonElement.serializer(), parsed)
        }.getOrElse { value }
    }

    private companion object {
        val TWELVE_DIGIT_TIMESTAMP = Regex("\\d{12}")
    }
}
