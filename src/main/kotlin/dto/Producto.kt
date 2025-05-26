package com.example.dto

data class Producto(
    val id_producto: Int,
    var nombre: String,
    var descripcion: String,
    var precio: Int,
    var stock: Int,
    var tipo_iva:Int,
    var categoria:Int
)