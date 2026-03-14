package org.example.project

data class DisabilityProfile(
    val wheelchair: Boolean = false,
    val hearingSensitivity: Boolean = false,
    val autismSensory: Boolean = false,
    val chronicPain: Boolean = false,
    val visualImpairment: Boolean = false
)

object ProfileStore {
    var profile: DisabilityProfile = DisabilityProfile()
}