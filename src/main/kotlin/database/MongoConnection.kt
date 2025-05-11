package com.crr.database

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase

class MongoConnection (private var connectionString:String="mongodb://dam:dam@localhost:27039") {
    private  var client: MongoClient? = null


    fun conect(){
        if(client==null)
            client = MongoClient.create(connectionString)
    }
    fun getDatabase(name:String): MongoDatabase? {
        if(client!=null)
            return client!!.getDatabase(name)
        else
            return null
    }
    fun isOpen(): Boolean {
        if (client==null)
            return false
        else
            return true
    }
    fun getConnectionString() :String{
        return connectionString;
    }
    fun setConnectionString(connectionString :String){
        this.connectionString = connectionString;
    }
    fun close(){
        client?.close()

    }

}