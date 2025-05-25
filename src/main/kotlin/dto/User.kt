package com.crr.users

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import javax.management.relation.Role

@Serializable
enum class Rol(){
    ADMIN,
    OPERATOR
}

@Serializable
data class User(
    var id: String? = null,
    var username: String = "",
    var password: String = "",
    var name: String = "",
    var role: Rol = Rol.OPERATOR,
) {
    constructor(): this(null, "", "","")
}

@Serializable
data class UserResponse(
    var id: String? = null,
    var username: String = ""
)


data class UserBson(
    @BsonId var _id: ObjectId? = null,
    var username: String = "",
    var password: String = "",
    var name: String = "",
    var role: Rol = Rol.OPERATOR
) {
    constructor(): this(null, "", "","",Rol.OPERATOR)
}

fun userToUserBson(obj: User): UserBson {
    val userBson = UserBson()
    userBson.username = obj.username
    userBson.password = obj.password
    userBson.name = obj.name
    userBson.role = obj.role
    return userBson
}