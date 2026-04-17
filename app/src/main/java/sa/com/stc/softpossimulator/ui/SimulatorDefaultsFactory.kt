package sa.com.stc.softpossimulator.ui

import android.content.Intent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import sa.com.stc.softpossimulator.model.ActionType
import sa.com.stc.softpossimulator.model.FailureMode
import sa.com.stc.softpossimulator.model.IncomingIntentSnapshot
import sa.com.stc.softpossimulator.model.ResultMode
import sa.com.stc.softpossimulator.model.SimulatorFormData
import sa.com.stc.softpossimulator.model.TransactionTypeOption
import sa.com.stc.softpossimulator.payload.SoftPosContract

object SimulatorDefaultsFactory {
    fun create(
        snapshot: IncomingIntentSnapshot,
        action: ActionType,
    ): SimulatorFormData {
        val requestOrderId = snapshot.extraMap[SoftPosContract.ExtraOrderId].orEmpty()
        val requestAmount = snapshot.extraMap[Intent.EXTRA_TEXT].orEmpty()
        val originalSequenceNumber = snapshot.extraMap[SoftPosContract.ExtraOriginalTransactionSequenceNumber].orEmpty()
        val originalTransactionDate = snapshot.extraMap[SoftPosContract.ExtraOriginalTransactionDate].orEmpty()
        val now = LocalDateTime.now()

        val base = SimulatorFormData(
            selectedAction = action,
            resultMode = ResultMode.SUCCESS,
            failureMode = FailureMode.DECLINED,
            orderId = requestOrderId.ifBlank { SoftPosContract.DefaultOrderId },
            amount = requestAmount.ifBlank { SoftPosContract.DefaultAmount },
            rrn = SoftPosContract.DefaultRrn,
            pan = SoftPosContract.DefaultPan,
            terminalId = SoftPosContract.DefaultTerminalId,
            cardName = SoftPosContract.DefaultCardName,
            authCode = "A12345",
            cardExpiryDate = SoftPosContract.DefaultCardExpiryDate,
            transactionRequestTime = REQUEST_TIME_FORMATTER.format(now),
            responseDescription = "Approved",
            responseCode = "051",
            abortReason = "User cancelled transaction",
            registrationFailureCode = "404",
            registrationFailureMessage = "Terminal is not registered",
            refundOriginalTransactionSequenceNumber = originalSequenceNumber.ifBlank { "000123" },
            refundOriginalTransactionDate = originalTransactionDate.ifBlank { "17-04-2026" },
            lastTransactionType = TransactionTypeOption.PURCHASE,
            lastTransactionStatus = SoftPosContract.StatusApproved,
            cardLabelName = SoftPosContract.DefaultCardName,
            authResponseCode = "A12345",
            transactionResponseDate = RESPONSE_DATE_FORMATTER.format(now),
            lastTransactionResponseCode = "00",
            lastTransactionFailureMessage = "No matching transaction found",
            reversalMessage = "Reversal declined",
        )

        return when (action) {
            ActionType.REGISTRATION_STATUS -> base.copy(
                orderId = SoftPosContract.DefaultOrderId,
                amount = SoftPosContract.DefaultAmount,
            )

            ActionType.PURCHASE -> base.copy(
                responseDescription = "Approved",
                responseCode = "051",
                abortReason = "User cancelled transaction",
                authCode = "A12345",
            )

            ActionType.REFUND -> base.copy(
                responseDescription = "Refund approved",
                responseCode = "055",
                abortReason = "Refund cancelled by user",
                authCode = "R12345",
            )

            ActionType.LAST_TRANSACTION_DETAILS -> base.copy(
                orderId = requestOrderId.ifBlank { SoftPosContract.DefaultOrderId },
                responseDescription = "Approved",
                lastTransactionType = TransactionTypeOption.PURCHASE,
                lastTransactionStatus = SoftPosContract.StatusApproved,
                lastTransactionResponseCode = "00",
            )

            ActionType.REVERSAL -> base.copy(
                orderId = requestOrderId.ifBlank { SoftPosContract.DefaultOrderId },
                responseCode = "400",
                abortReason = "Reversal cancelled by user",
            )
        }
    }

    private val REQUEST_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmss")
    private val RESPONSE_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
}
