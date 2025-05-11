package com.example

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.crr.database.MongoConnection
import com.crr.users.User
import com.crr.users.UserResponse
import com.crr.users.userToUserBson
import com.example.repositorys.users.UserRepository
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val mongoConnection: MongoConnection = MongoConnection()
    val userRepository: UserRepository = UserRepository(mongoConnection)
    val myRealm = environment.config.property("jwt.realm").getString()
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()

    install(Authentication) {
        jwt("jwt-auth") {
            realm = myRealm
            // Verifica que el token sea un token válido así como la signatura
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )

            // Valida el payload
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            // Configura una respuesta cuando la autenticación falle
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Token is not valid or has expired"
                )
            }
        }
    }

    routing {
        get("/") {
            val users = userRepository.getAll()
            val usersResponses = mutableListOf<UserResponse>()
            users.forEach {
                val userResponse = UserResponse(
                    id = it._id.toString(),
                    username = it.username,
                )
                usersResponses.add(userResponse)
            }
            call.respond(
                HttpStatusCode.OK,
                usersResponses
            )
        }
        post("/users") {
            try {
                var userExist = false
                val userSer = call.receive<User>()
                val bcryptHashPassword = BCrypt.withDefaults().hashToString(12, userSer.password.toCharArray());
                val usersMongo = userRepository.getAll()
                if (usersMongo != null) {
                    usersMongo.forEach {
                        if (it.username == userSer.username) {
                            userExist = true
                        }
                    }
                }
                if (!userExist){
                    userSer.password = bcryptHashPassword
                    userRepository.add(userToUserBson(userSer))
                    call.respond(
                        HttpStatusCode.Created,
                        "Usuario creado"
                    )
                } else {
                    call.respond(HttpStatusCode.Conflict, "Usuario ya existente")
                }

            } catch (e: IllegalStateException) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = mapOf("message" to e.message)
                )
            } catch (e: JsonConvertException) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = mapOf("message" to e.message)
                )
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = mapOf("message" to e.message)
                )
            }
        }
        staticResources("/static", "static")
    }
}
