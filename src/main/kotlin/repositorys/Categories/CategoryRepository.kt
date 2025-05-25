package com.example.repositorys.Category

import com.crr.users.Category
import com.crr.users.Forma_pago
import com.example.EntidadesMySQL.CategoryMySQL
import com.example.EntidadesMySQL.FormaPagoMySQL
import org.bson.types.ObjectId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class CategoryRepository(){
    fun getAll(): List<Category>{
        return transaction {
                CategoryMySQL.selectAll().map {
                Category(
                    nombre = it[CategoryMySQL.categoryName],
                    id_category = it[CategoryMySQL.id]
                )
            }
        }
    }
}