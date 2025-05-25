package com.example.repositorys.Category

import com.example.EntidadesMySQL.InvoicesMySQL
import com.example.dto.Invoice
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class InvoiceRepository(){
    fun getAll(): List<Invoice>{
        return transaction {
                InvoicesMySQL.selectAll().map {
                Invoice(
                    id_invoice = it[InvoicesMySQL.id],
                    numserie = it[InvoicesMySQL.numSerie],
                    fechaEmision = it[InvoicesMySQL.fechaEmision],
                    total_iva = it[InvoicesMySQL.totalIva],
                    estado = it[InvoicesMySQL.estado],
                    firma_hash = it[InvoicesMySQL.firmaHash],
                    hash_anterior = it[InvoicesMySQL.hashAnterior],
                    algoritmo_cifrado = it[InvoicesMySQL.algoritmoCifrado],
                    operador = it[InvoicesMySQL.operador],
                    forma_pago = it[InvoicesMySQL.formaPagoId]
                )
            }
        }
    }
}