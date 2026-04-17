package sa.com.stc.softpossimulator.model

enum class ActionType(
    val wireAction: String,
    val label: String,
) {
    REGISTRATION_STATUS(
        wireAction = "geidea.net.softpos.REGISTRATION_STATUS",
        label = "REGISTRATION_STATUS",
    ),
    LAST_TRANSACTION_DETAILS(
        wireAction = "geidea.net.softpos.LASTTRANSACTIONDETAILS",
        label = "LASTTRANSACTIONDETAILS",
    ),
    REFUND(
        wireAction = "geidea.net.softpos.REFUND",
        label = "REFUND",
    ),
    PURCHASE(
        wireAction = "geidea.net.softpos.PURCHASE",
        label = "PURCHASE",
    ),
    REVERSAL(
        wireAction = "geidea.net.softpos.REVERSAL",
        label = "REVERSAL",
    ),
    ;

    val supportsFailureBranchSelector: Boolean
        get() = this == PURCHASE || this == REFUND || this == REVERSAL

    companion object {
        fun fromAction(action: String?): ActionType? =
            entries.firstOrNull { it.wireAction == action }
    }
}
