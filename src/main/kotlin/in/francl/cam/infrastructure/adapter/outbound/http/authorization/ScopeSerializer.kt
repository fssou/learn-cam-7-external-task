package `in`.francl.cam.infrastructure.adapter.outbound.http.authorization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class ScopeSerializer : KSerializer<Set<String>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("scope", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Set<String>) {
        val scopeString = value.joinToString(" ")
        encoder.encodeString(scopeString)
    }

    override fun deserialize(decoder: Decoder): Set<String> {
        val scopeString = decoder.decodeString()
        return scopeString.split(" ").toSet()
    }
}