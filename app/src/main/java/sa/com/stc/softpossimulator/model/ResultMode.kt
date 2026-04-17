package sa.com.stc.softpossimulator.model

enum class ResultMode(val label: String) {
    SUCCESS("Success"),
    FAILURE("Failure"),
}

enum class FailureMode(
    val label: String,
    val statusValue: String,
) {
    DECLINED(
        label = "Declined",
        statusValue = "Declined",
    ),
    ABORTED(
        label = "Aborted",
        statusValue = "Aborted",
    ),
}

enum class TransactionTypeOption(val wireValue: String) {
    PURCHASE("Purchase"),
    REFUND("Refund"),
}
