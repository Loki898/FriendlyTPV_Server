package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class Producto(
    val id_producto: Int?=0,
    var nombre: String?="",
    var descripcion: String?="",
    var precio: Double?=0.0,
    var stock: Int?=0,
    var tipo_iva:Int?=0,
    var categoria:Int?=0
)