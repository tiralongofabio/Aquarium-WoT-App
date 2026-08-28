package it.uniboft.aquarium.data.repositories


import it.uniboft.aquarium.domain.repositories.ITotpGenerator
import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.math.pow


class TotpGeneratorImpl @Inject constructor() : ITotpGenerator {


    override fun generateCurrentToken(secretBase32: String): String {
        // 1. Il TOTP cambia ogni 30 secondi
        val timeStep = System.currentTimeMillis() / 1000 / 30

        // 2. Decodifica il secret da Base32 a ByteArray
        val base32 = Base32()
        val keyBytes = base32.decode(secretBase32.uppercase())

        // 3. Prepara il payload del tempo (8 byte)
        val timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array()

        // 4. Genera l'hash HMAC-SHA1
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(keyBytes, "HmacSHA1"))
        val hash = mac.doFinal(timeBytes)

        // 5. Estrazione dinamica (Dynamic Truncation) RFC 4226
        val offset = hash.last().toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)

        // 6. Ritorna il token a 6 cifre con padding di zeri
        val otp = binary % 10.0.pow(6).toInt()
        return otp.toString().padStart(6, '0')
    }
}
