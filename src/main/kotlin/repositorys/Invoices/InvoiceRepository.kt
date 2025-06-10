package com.example.repositorys.Category

import com.example.EntidadesMySQL.InvoicesMySQL
import com.example.dto.Invoice
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class InvoiceRepository {
    fun getAll(): List<Invoice> {
        return transaction {
            InvoicesMySQL.selectAll().map {
                Invoice(
                    idInvoice = it[InvoicesMySQL.id],
                    numSerie = it[InvoicesMySQL.numSerie],
                    fechaEmision = it[InvoicesMySQL.fechaEmision],
                    baseImponible = it[InvoicesMySQL.baseImponible],
                    totalIva = it[InvoicesMySQL.totalIva],
                    total = it[InvoicesMySQL.total],
                    estado = it[InvoicesMySQL.estado],
                    firmaHash = it[InvoicesMySQL.firmaHash],
                    hashAnterior = it[InvoicesMySQL.hashAnterior],
                    algoritmoCifrado = it[InvoicesMySQL.algoritmoCifrado],
                    operador = it[InvoicesMySQL.operador],
                    formaPago = it[InvoicesMySQL.formaPagoId]
                )
            }
        }
    }
}