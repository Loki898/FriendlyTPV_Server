package com.example.EntidadesMySQL

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime


object FormaPagoMySQL : Table("forma_pago") {
    val id = integer("id_formpago").autoIncrement()
    val nombre = varchar("descripcion", 30)

    override val primaryKey = PrimaryKey(id)
}

object CategoryMySQL : Table("category") {
    val id = integer("id_categoria").autoIncrement()
    val categoryName = varchar("category_name", 30)

    override val primaryKey = PrimaryKey(id)
}

object InvoicesMySQL : Table("invoices") {
    val id = integer("id_invoice")
    val numSerie = integer("num_serie")
    val fechaEmision = datetime("fecha_emision").nullable()
    val baseImponible = double("base_imponible").nullable()
    val totalIva = double("total_iva").nullable()
    val total = double("total").nullable()
    val estado = integer("estado").nullable()
    val firmaHash = text("firma_hash").nullable()
    val hashAnterior = text("hash_anterior").nullable()
    val algoritmoCifrado = text("algoritmo_cifrado").nullable()
    val operador = varchar("operador", 30).nullable()
    val formaPagoId = integer("id_formapago").references(FormaPagoMySQL.id)

    override val primaryKey = PrimaryKey(id)
}


object ProductsMySQL : Table("products") {
    val id = integer("id_producto").autoIncrement()
    val nombre = varchar("nombre", 30).nullable()
    val descripcion = varchar("descripcion", 100).nullable()
    val precio = double("precio").nullable()
    val stock = integer("stock").nullable()
    val tipoIva = integer("tipo_iva").nullable()
    val categoryId = integer("category_id").references(CategoryMySQL.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id)
}


object LineasVentaMySQL : Table("sales_lines") {
    val numeroLinea = integer("numero_linea")
    val ventaId = integer("venta_id").references(InvoicesMySQL.id)
    val producto = integer("producto").references(ProductsMySQL.id)
    val precioUni = double("precio_uni").nullable()
    val cantidad = integer("cantidad").nullable()
    val subtotal = double("subtotal").nullable()
    val tipoIva = integer("tipo_iva").nullable()

    override val primaryKey = PrimaryKey(numeroLinea, ventaId, name = "PK_LineasVenta")
}




