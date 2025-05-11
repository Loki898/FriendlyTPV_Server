package com.example.repositorys.users

import com.crr.database.MongoConnection
import com.crr.users.UserBson
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.BsonValue
import org.bson.types.ObjectId

class UserRepository(private val mongoConnection: MongoConnection): AUserRepository() {
    private val dbName="friendlytpv"
    private val collectionName="users"

    override suspend fun getAll(): List<UserBson> {
        if(!mongoConnection.isOpen()){
            mongoConnection.conect()
        }
        val db = mongoConnection.getDatabase(dbName)
        db?.let{
            val collection = it.getCollection<UserBson>(collectionName)
            val doc = collection.find()
            return doc.toList()
        }
        return emptyList()
    }
    suspend fun getByUsername(username:String): UserBson?{
        if(!mongoConnection.isOpen()) {
            mongoConnection.conect()
        }
        val db = mongoConnection.getDatabase(dbName)
        db?.let {
            val collection = it.getCollection<UserBson>(collectionName)
            val query = Filters.eq("username", username)
            val r = collection.find(query)
            return r.firstOrNull()
        }
        return null
    }

    override suspend fun remove(item: UserBson) {
        item._id?.let { this.removeById(it) }
    }

    override suspend fun removeById(id: ObjectId) {
        if (!mongoConnection.isOpen()) {
            mongoConnection.conect()
        }
        val db = mongoConnection.getDatabase(dbName)
        db?.let {
            val collection = it.getCollection<UserBson>(collectionName)
            val query = Filters.eq("_id", id)
            var r = collection.deleteOne(query)
        }
    }

    override suspend fun getById(id: ObjectId):UserBson? {
        if (!mongoConnection.isOpen()) {
            mongoConnection.conect()
        }
        val db = mongoConnection.getDatabase(dbName)
        db?.let {
            val collection = it.getCollection<UserBson>(collectionName)
            val query = Filters.eq("_id", id)
            val user = collection.find(query)
            return user.firstOrNull()
        }
        return null
    }

    override suspend fun update(item: UserBson) {
        val db = mongoConnection.getDatabase(dbName)
        db?.let {
            val collection = it.getCollection<UserBson>(collectionName)
            val query = Filters.eq("_id", item._id)
            var bson= Updates.combine(
                Updates.set("username",item.username),
                Updates.set("password",item.password),
                Updates.set("name",item.name),
                Updates.set("rol", item.role)
            )
            collection.findOneAndUpdate(query,bson)
        }
    }

    override suspend fun updateById(item: UserBson, id: ObjectId) {
        val db = mongoConnection.getDatabase(dbName)
        db?.let {
            val collection = it.getCollection<UserBson>(collectionName)
            val query = Filters.eq("_id", id)
            var bson= Updates.combine(
                Updates.set("username",item.username),
                Updates.set("password",item.password),
                Updates.set("name",item.name),
                Updates.set("rol", item.role)
            )
            collection.findOneAndUpdate(query,bson)
        }
    }

    override suspend fun save(item: UserBson) {
        if(item._id==null) {
            //se le pone un id nuevo
            item._id= ObjectId()
            this.add(item)
        }else
            this.update(item)
    }

    override suspend fun add(item: UserBson): String? {
        if (!mongoConnection.isOpen()) {
            mongoConnection.conect()
        }
        val db = mongoConnection.getDatabase(dbName)
        db?.let {
            val collection = it.getCollection<UserBson>(collectionName)
            val result = collection.insertOne(item)
            return result.insertedId.toString()
        }
        return null
    }
}