package com.example.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class Invoice(
    val idInvoice: Int? = null,
    val numSerie: Int? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val fechaEmision: LocalDateTime? = null,
    val baseImponible: Double? = null,
    val totalIva: Double? = null,
    val total: Double? = null,
    val estado: Int? = 0,
    val firmaHash: String? = null,
    val hashAnterior: String? = null,
    val algoritmoCifrado: String? = null,
    val operador: String? = null,
    val formaPago: Int? = null
)

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        val string = value.format(formatter)
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val string = decoder.decodeString()
        return LocalDateTime.parse(string, formatter)
    }
}