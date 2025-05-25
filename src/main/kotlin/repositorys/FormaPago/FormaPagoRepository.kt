package com.example.repositorys.FormaPago

import com.crr.users.Forma_pago
import com.example.EntidadesMySQL.FormaPagoMySQL
import org.bson.types.ObjectId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class FormaPagoRepository(){
    fun getAll(): List<Forma_pago>{
        return transaction {
            FormaPagoMySQL.selectAll().map {
                Forma_pago(
                    nombre = it[FormaPagoMySQL.nombre],
                    id_formapago = it[FormaPagoMySQL.id]
                )
            }
        }
    }
}