package com.example.dto

import com.example.EntidadesMySQL.FormaPagoMySQL
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Forma_pago (
    var id_formapago:Int? = null,
    var nombre:String? = null,
){
    constructor(): this(0,"")
}

fun obtenerFormasPago(): List<Forma_pago>{
    return transaction {
        FormaPagoMySQL.selectAll().map {
            Forma_pago(
                nombre = it[FormaPagoMySQL.nombre],
                id_formapago = it[FormaPagoMySQL.id]
            )
        }
    }
}
fun createFormaPago(name: String): Int = transaction {
    FormaPagoMySQL.insert {
        it[FormaPagoMySQL.nombre] = name
    } get FormaPagoMySQL.id
}
