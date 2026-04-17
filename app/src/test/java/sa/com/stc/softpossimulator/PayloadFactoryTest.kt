package sa.com.stc.softpossimulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sa.com.stc.softpossimulator.model.ActionType
import sa.com.stc.softpossimulator.model.FailureMode
import sa.com.stc.softpossimulator.model.ResultMode
import sa.com.stc.softpossimulator.model.SimulatorFormData
import sa.com.stc.softpossimulator.payload.PayloadFactory
import sa.com.stc.softpossimulator.payload.SoftPosContract
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PayloadFactoryTest {
    private val payloadFactory = PayloadFactory()
    private val json = Json

    @Test
    fun `registration success and failure use expected top level extras`() {
        val success = payloadFactory.build(
            SimulatorFormData(
                selectedAction = ActionType.REGISTRATION_STATUS,
                resultMode = ResultMode.SUCCESS,
                terminalId = "12345678TERM0001",
            ),
        )

        assertEquals(SoftPosContract.StatusSuccess, success.payload.topLevelExtras[SoftPosContract.ExtraStatus])
        val successData = json.parseToJsonElement(
            success.payload.topLevelExtras.getValue(SoftPosContract.ExtraData),
        ).jsonObject
        assertEquals("12345678TERM0001", successData.getValue("terminal_id").jsonPrimitive.content)

        val failure = payloadFactory.build(
            SimulatorFormData(
                selectedAction = ActionType.REGISTRATION_STATUS,
                resultMode = ResultMode.FAILURE,
                registrationFailureCode = "404",
                registrationFailureMessage = "Terminal is not registered",
            ),
        )

        assertEquals(SoftPosContract.StatusFailed, failure.payload.topLevelExtras[SoftPosContract.ExtraStatus])
        val failureResult = json.parseToJsonElement(
            failure.payload.topLevelExtras.getValue(SoftPosContract.ExtraResult),
        ).jsonObject
        assertEquals(404, failureResult.getValue("code").jsonPrimitive.int)
        assertEquals("Terminal is not registered", failureResult.getValue("message").jsonPrimitive.content)
    }

    @Test
    fun `purchase covers approved declined and aborted responses`() {
        val approved = payloadFactory.build(
            SimulatorFormData(
                selectedAction = ActionType.PURCHASE,
                resultMode = ResultMode.SUCCESS,
                orderId = "ORDER-1001",
                amount = "115.00",
                rrn = "123456789012",
                pan = "541234******1111",
                terminalId = "12345678TERM0001",
                cardName = "MASTERCARD",
                authCode = "A12345",
                cardExpiryDate = "12/28",
                transactionRequestTime = "260417143500",
                responseDescription = "Approved",
            ),
        )

        assertEquals(SoftPosContract.StatusApproved, approved.payload.topLevelExtras[SoftPosContract.ExtraStatus])
        val approvedResult = json.parseToJsonElement(
            approved.payload.topLevelExtras.getValue(SoftPosContract.ExtraResult),
        ).jsonObject
        assertEquals("ORDER-1001", approvedResult.getValue("ORDER_ID").jsonPrimitive.content)
        assertEquals("Approved", approvedResult.getValue("res_desc").jsonPrimitive.content)

        val declined = payloadFactory.build(
            SimulatorFormData(
                selectedAction = ActionType.PURCHASE,
                resultMode = ResultMode.FAILURE,
                failureMode = FailureMode.DECLINED,
                responseCode = "051",
                responseDescription = "Declined by issuer",
            ),
        )

        assertEquals(SoftPosContract.StatusDeclined, declined.payload.topLevelExtras[SoftPosContract.ExtraStatus])
        val declinedResult = json.parseToJsonElement(
            declined.payload.topLevelExtras.getValue(SoftPosContract.ExtraResult),
        ).jsonObject
        assertEquals("051", declinedResult.getValue("resp_code").jsonPrimitive.content)
        assertEquals("Declined by issuer", declinedResult.getValue("res_desc").jsonPrimitive.content)

        val aborted = payloadFactory.build(
            SimulatorFormData(
                selectedAction = ActionType.PURCHASE,
                resultMode = ResultMode.FAILURE,
                failureMode = FailureMode.ABORTED,
                abortReason = "User cancelled transaction",
            ),
        )

        assertEquals(SoftPosContract.StatusAborted, aborted.payload.topLevelExtras[SoftPosContract.ExtraStatus])
        assertEquals("User cancelled transaction", aborted.payload.topLevelExtras[SoftPosContract.ExtraReason])
    }

    @Test
    fun `refund success matches purchase contract shape`() {
        val result = payloadFactory.build(
            SimulatorFormData(
                selectedAction = ActionType.REFUND,
                resultMode = ResultMode.SUCCESS,
                orderId = "ORDER-1001",
                amount = "115.00",
                rrn = "123456789012",
                pan = "541234******1111",
                terminalId = "12345678TERM0001",
                cardName = "MASTERCARD",
                authCode = "R12345",
                cardExpiryDate = "12/28",
                transactionRequestTime = "260417143500",
                responseDescription = "Refund approved",
            ),
        )

        assertEquals(SoftPosContract.StatusApproved, result.payload.topLevelExtras[SoftPosContract.ExtraStatus])
        val refundJson = json.parseToJsonElement(
            result.payload.topLevelExtras.getValue(SoftPosContract.ExtraResult),
        ).jsonObject
        assertEquals("ORDER-1001", refundJson.getValue("ORDER_ID").jsonPrimitive.content)
        assertEquals("R12345", refundJson.getValue("auth_code").jsonPrimitive.content)
        assertEquals("Refund approved", refundJson.getValue("res_desc").jsonPrimitive.content)
    }

    @Test
    fun `last transaction success stringifies nested message json`() {
        val result = payloadFactory.build(
            SimulatorFormData(
                selectedAction = ActionType.LAST_TRANSACTION_DETAILS,
                resultMode = ResultMode.SUCCESS,
                orderId = "ORDER-1001",
                amount = "115.00",
                rrn = "123456789012",
                pan = "541234******1111",
                terminalId = "12345678TERM0001",
                cardLabelName = "MASTERCARD",
                authResponseCode = "A12345",
                cardExpiryDate = "12/28",
                transactionResponseDate = "17-04-2026 14:35:00",
                lastTransactionResponseCode = "00",
            ),
        )

        assertEquals(SoftPosContract.StatusSuccess, result.payload.topLevelExtras[SoftPosContract.ExtraStatus])
        val outerResult = json.parseToJsonElement(
            result.payload.topLevelExtras.getValue(SoftPosContract.ExtraResult),
        ).jsonObject
        val nestedMessage = outerResult.getValue("message").jsonPrimitive.content
        val parsedMessage = json.parseToJsonElement(nestedMessage).jsonObject
        assertEquals("Approved", parsedMessage.getValue("transaction_status").jsonPrimitive.content)
        assertEquals("ORDER-1001", parsedMessage.getValue("order_id").jsonPrimitive.content)
        assertEquals("00", outerResult.getValue("resp_code").jsonPrimitive.content)
    }

    @Test
    fun `reversal supports approved declined and aborted top level statuses`() {
        val approved = payloadFactory.build(
            SimulatorFormData(
                selectedAction = ActionType.REVERSAL,
                resultMode = ResultMode.SUCCESS,
                orderId = "ORDER-1001",
            ),
        )
        assertEquals(SoftPosContract.StatusApproved, approved.payload.topLevelExtras[SoftPosContract.ExtraStatus])

        val declined = payloadFactory.build(
            SimulatorFormData(
                selectedAction = ActionType.REVERSAL,
                resultMode = ResultMode.FAILURE,
                failureMode = FailureMode.DECLINED,
                orderId = "ORDER-1001",
                responseCode = "400",
                reversalMessage = "Reversal declined",
            ),
        )
        assertEquals(SoftPosContract.StatusDeclined, declined.payload.topLevelExtras[SoftPosContract.ExtraStatus])
        val declinedResult = json.parseToJsonElement(
            declined.payload.topLevelExtras.getValue(SoftPosContract.ExtraResult),
        ).jsonObject
        assertEquals("400", declinedResult.getValue("resp_code").jsonPrimitive.content)
        assertEquals("Reversal declined", declinedResult.getValue("message").jsonPrimitive.content)

        val aborted = payloadFactory.build(
            SimulatorFormData(
                selectedAction = ActionType.REVERSAL,
                resultMode = ResultMode.FAILURE,
                failureMode = FailureMode.ABORTED,
                orderId = "ORDER-1001",
                abortReason = "Reversal cancelled by user",
            ),
        )
        assertEquals(SoftPosContract.StatusAborted, aborted.payload.topLevelExtras[SoftPosContract.ExtraStatus])
        assertEquals("Reversal cancelled by user", aborted.payload.topLevelExtras[SoftPosContract.ExtraReason])
    }

    @Test
    fun `validation rejects short terminal ids for success payloads`() {
        val result = payloadFactory.build(
            SimulatorFormData(
                selectedAction = ActionType.PURCHASE,
                resultMode = ResultMode.SUCCESS,
                orderId = "ORDER-1001",
                terminalId = "1234567",
                transactionRequestTime = "260417143500",
            ),
        )

        assertTrue(result.validationErrors.any { it.contains("terminal_id") })
    }
}
