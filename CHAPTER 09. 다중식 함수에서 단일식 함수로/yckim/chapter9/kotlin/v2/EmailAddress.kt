package chapter9.kotlin.v2

data class EmailAddress(
    val localPart: String,
    val domain: String
) {
    override fun toString(): String {
        return "$localPart@$domain"
    }

    companion object {
        @JvmStatic
        fun parse(value: String): EmailAddress = emailAddress(value.lastIndexOf('@'), value)

        private fun emailAddress(atIndex: Int, value: String): EmailAddress =
            when {
                atIndex < 1 || atIndex == value.length - 1 -> {
                    throw IllegalArgumentException("EmailAddress must be two parts separated by @")
                }

                else -> {
                    EmailAddress(
                        value.substring(0, atIndex),
                        value.substring(atIndex + 1)
                    )
                }
            }
    }
}