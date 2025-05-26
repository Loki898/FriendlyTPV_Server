package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class LineaVenta(
    var numeroLinea:Int,
    var venda_id:Int,
    var producto:Int,
    var precio_uni:Double?,
    var cantidad:Int? = 0,
    var subtotal:Double?,
    var tipo_iva:Int? = 21
){
}