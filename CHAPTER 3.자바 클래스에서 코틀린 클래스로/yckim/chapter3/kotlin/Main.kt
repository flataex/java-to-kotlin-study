package chapter3.kotlin

import chapter3.kotlin.email.v3.EmailAddress

fun main() {
    val customerEmail = EmailAddress.parse("customer@mail.com")
    val postMasterEmail = customerEmail.copy(localPart = "postMaster")
    println(customerEmail)
    println(postMasterEmail)
}