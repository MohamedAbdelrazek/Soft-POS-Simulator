package sa.com.stc.softpossimulator.payload

object SoftPosContract {
    const val MimeTypeTextPlain = "text/plain"

    const val ExtraStatus = "status"
    const val ExtraData = "data"
    const val ExtraResult = "result"
    const val ExtraReason = "reason"

    const val ExtraOrderId = "ORDER_ID"
    const val ExtraOriginalTransactionSequenceNumber = "ORIGINAL_TRANS_SEQ_NO"
    const val ExtraOriginalTransactionDate = "ORIGINAL_TRANS_DATE"

    const val StatusSuccess = "Success"
    const val StatusFailed = "Failed"
    const val StatusApproved = "Approved"
    const val StatusDeclined = "Declined"
    const val StatusAborted = "Aborted"

    const val DefaultTerminalId = "12345678TERM0001"
    const val DefaultOrderId = "ORDER-1001"
    const val DefaultAmount = "115.00"
    const val DefaultRrn = "123456789012"
    const val DefaultPan = "541234******1111"
    const val DefaultCardName = "MASTERCARD"
    const val DefaultCardExpiryDate = "12/28"
}
