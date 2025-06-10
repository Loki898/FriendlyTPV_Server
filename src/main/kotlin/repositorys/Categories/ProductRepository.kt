package com.example.repositorys.Category


import com.example.EntidadesMySQL.CategoryMySQL
import com.example.EntidadesMySQL.ProductsMySQL
import com.example.dto.Category
import com.example.dto.Producto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ProductRepository {

    fun insertProduct(product: Producto): Int {
        return transaction {
            ProductsMySQL.insert {
                it[nombre] = product.nombre
                it[descripcion] = product.descripcion
                it[precio] = product.precio
                it[stock] = product.stock
                it[categoryId]= product.categoria!!
            } get ProductsMySQL.id
        }
    }

    fun getProductsByCategory(category: Int): List<Producto> {
        return transaction {
            ProductsMySQL.select{ProductsMySQL.categoryId eq category}.mapNotNull {
                Producto(
                    nombre = it[ProductsMySQL.nombre],
                    id_producto = it[ProductsMySQL.id],
                    descripcion = it[ProductsMySQL.descripcion],
                    precio = it[ProductsMySQL.precio],
                    stock = it[ProductsMySQL.stock],
                    tipo_iva = it[ProductsMySQL.tipoIva],
                    categoria = it[ProductsMySQL.categoryId]
                )
            }
        }
    }

    fun getAll(): List<Producto> {
        return transaction {
            ProductsMySQL.selectAll().map {
                Producto(
                    nombre = it[ProductsMySQL.nombre],
                    descripcion = it[ProductsMySQL.descripcion],
                    precio = it[ProductsMySQL.precio],
                    stock = it[ProductsMySQL.stock],
                    tipo_iva = it[ProductsMySQL.tipoIva],
                    categoria = it[ProductsMySQL.categoryId],
                    id_producto = it[ProductsMySQL.id]
                )
            }
        }
    }

    fun getById(id: Int): Category? {
        return transaction {
            CategoryMySQL.select { CategoryMySQL.id eq id }.mapNotNull {
                    Category(
                        id_category = it[CategoryMySQL.id], nombre = it[CategoryMySQL.categoryName]
                    )
                }.singleOrNull()
        }
    }

    fun getProductByName(name: String): Producto? {
        return transaction {
            ProductsMySQL.select { ProductsMySQL.nombre eq name }.mapNotNull {
                Producto(
                    id_producto = it[ProductsMySQL.id],
                    nombre = it[ProductsMySQL.nombre],
                    descripcion = it[ProductsMySQL.descripcion],
                    precio = it[ProductsMySQL.precio],
                    stock = it[ProductsMySQL.stock],
                    tipo_iva = it[ProductsMySQL.tipoIva],
                    categoria = it[ProductsMySQL.categoryId],
                )
            }.singleOrNull()
        }
    }

    fun updateCategory(category: Category): Boolean {
        return transaction {
            CategoryMySQL.update({ CategoryMySQL.id eq category.id_category }) {
                it[categoryName] = category.nombre
            } > 0
        }
    }

    fun deleteById(id: Int): Boolean {
        return transaction {
            CategoryMySQL.deleteWhere { CategoryMySQL.id eq id } > 0
        }
    }
}