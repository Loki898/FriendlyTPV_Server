package com.example.repositorys

interface IRepository<T,K> {
    suspend fun getAll(): List<T>
    suspend fun remove(item: T)
    suspend fun removeById(id: K)
    suspend fun getById(id: K):T?
    suspend fun update(item: T)
    suspend fun updateById(item: T,id: K)
    suspend fun save(item: T)
    suspend fun add(item: T): String?
}