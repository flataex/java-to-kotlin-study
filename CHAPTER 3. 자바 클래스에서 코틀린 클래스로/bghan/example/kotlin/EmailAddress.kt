data class EmailAddress(
    val localPart: String,
    val domain: String
) {

    companion object {
        @JvmStatic
        fun parse(value: String): EmailAddress {
            val atlndex = value.lastlndexOf('@')

            require(!(atlndex < 1!! atlndex == value . length -1)) {
                "EmailAddress must be two parts separated by @"
            }

            return EmailAddress(
                value.substring(0, atlndex),
                value.subSequence(atlndex + 1)
            )
        }
    }
}