package com.example.repositorys.Category


import com.example.EntidadesMySQL.CategoryMySQL
import com.example.dto.Category
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class CategoryRepository {

    fun insertCategory(category: Category): Int {
        return transaction {
            CategoryMySQL.insert {
                it[categoryName] = category.nombre
            } get CategoryMySQL.id
        }
    }

    fun getAll(): List<Category> {
        return transaction {
            CategoryMySQL.selectAll().map {
                Category(
                    nombre = it[CategoryMySQL.categoryName], id_category = it[CategoryMySQL.id]
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

    fun getCategorieByName(name: String): Category? {
        return transaction {
            CategoryMySQL.select { CategoryMySQL.categoryName eq name }.mapNotNull {
                Category(
                    id_category = it[CategoryMySQL.id], nombre = it[CategoryMySQL.categoryName]
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