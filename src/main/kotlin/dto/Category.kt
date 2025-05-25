package com.crr.users

import com.example.EntidadesMySQL.FormaPagoMySQL
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Category (
    var id_category:Int? = null,
    var nombre:String? = null,
){
    constructor(): this(0,"")
}

