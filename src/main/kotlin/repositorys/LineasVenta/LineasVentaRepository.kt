package com.example.repositorys.LineasVenta

import com.example.EntidadesMySQL.LineasVentaMySQL
import com.example.dto.Category
import com.example.dto.LineaVenta
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class LineasVentaRepository{

    fun insert(category: Category){
        transaction {

        }
    }

    fun getAll(): List<LineaVenta>{
        return transaction {
            LineasVentaMySQL.selectAll().map {
                LineaVenta(
                    numeroLinea = it[LineasVentaMySQL.numeroLinea],
                    venda_id = it[LineasVentaMySQL.ventaId],
                    producto = it[LineasVentaMySQL.producto],
                    precio_uni = it[LineasVentaMySQL.precioUni],
                    cantidad = it[LineasVentaMySQL.cantidad],
                    subtotal = it[LineasVentaMySQL.subtotal],
                    tipo_iva = it[LineasVentaMySQL.tipoIva],
                )
            }
        }
    }
}