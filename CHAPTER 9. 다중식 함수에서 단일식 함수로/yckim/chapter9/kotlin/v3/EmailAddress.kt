package chapter9.kotlin.v3

data class EmailAddress(
    val localPart: String,
    val domain: String
) {
    override fun toString(): String {
        return "$localPart@$domain"
    }

    companion object {
        @JvmStatic
        fun parse(value: String): EmailAddress =
            value.lastIndexOf('@').let { atIndex ->
                require(!(atIndex < 1 || atIndex == value.length - 1)) {
                    "EmailAddress must be two parts separated by @"
                }
                EmailAddress(
                    value.substring(0, atIndex),
                    value.substring(atIndex + 1)
                )
            }
        
    }
}