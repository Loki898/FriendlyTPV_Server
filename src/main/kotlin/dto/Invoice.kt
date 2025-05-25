package com.example.dto

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import java.time.format.DateTimeFormatter

@Serializable
data class Invoice(
    var id_invoice:Int? = null,
    var numserie:Int? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val fechaEmision: LocalDateTime?,
    var total_iva: Double? = null,
    var estado:Int? = 0,
    var firma_hash:String?=null,
    var hash_anterior:String?=null,
    var algoritmo_cifrado:String?=null,
    var operador:Int?=null,
    var forma_pago:Int?=null
    ){}


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