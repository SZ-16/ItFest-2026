package org.example.project

enum class Screen {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    WELCOME,
    DISABILITY_SELECT,
    MAIN
}

object CurrentUser {
    var userId: Int = -1
    var username: String = ""
    var email: String = ""
    var profile: DisabilityProfile = DisabilityProfile()
}