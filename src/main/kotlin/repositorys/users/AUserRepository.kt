package com.example.repositorys.users

import com.crr.users.UserBson
import com.example.repositorys.IRepository
import org.bson.types.ObjectId

abstract class AUserRepository: IRepository<UserBson, ObjectId> {
}