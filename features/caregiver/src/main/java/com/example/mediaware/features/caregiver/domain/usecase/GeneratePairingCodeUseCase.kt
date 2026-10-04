package com.example.mediaware.features.caregiver.domain.usecase

import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.caregiver.domain.model.PairingCode
import java.security.SecureRandom
import javax.inject.Inject

/**
 * UseCase to generate a cryptographically secure 6-digit time-bound pairing handshake.
 * Default TTL is 600 seconds (10 minutes) for rural patient-caregiver device linkage.
 */
class GeneratePairingCodeUseCase @Inject constructor() {

    operator fun invoke(
        userId: String = "user_primary",
        ttlSeconds: Long = 600L
    ): PairingCode {
        val random = SecureRandom()
        // Generate secure 6-digit integer from 100000 to 999999
        val codeNumber = 100_000 + random.nextInt(900_000)
        val codeString = codeNumber.toString()

        val expiresAt = System.currentTimeMillis() + (ttlSeconds * 1000L)

        // Format as spaced digits in Bengali: e.g. "৫৮২ ৯১৪"
        val part1 = codeString.substring(0, 3).toBengaliDigits()
        val part2 = codeString.substring(3, 6).toBengaliDigits()
        val formattedBn = "$part1 $part2"

        val deepLinkUri = "mediaware://pair?code=$codeString&uid=$userId&exp=$expiresAt"

        return PairingCode(
            code = codeString,
            expiresAt = expiresAt,
            ttlSeconds = ttlSeconds,
            formattedCodeBn = formattedBn,
            deepLinkUri = deepLinkUri
        )
    }
}
