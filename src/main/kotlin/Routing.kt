package com.example

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.crr.database.MongoConnection
import com.example.database.DatabaseFactory
import com.example.dto.*
import com.example.repositorys.Category.CategoryRepository
import com.example.repositorys.Category.InvoiceRepository
import com.example.repositorys.Category.ProductRepository
import com.example.repositorys.FormaPago.FormaPagoRepository
import com.example.repositorys.LineasVenta.LineasVentaRepository
import com.example.repositorys.users.UserRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.mongodb.DuplicateKeyException
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.bson.types.ObjectId
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

fun Application.configureRouting() {
    val mongoConnection: MongoConnection = MongoConnection()
    val userRepository: UserRepository = UserRepository(mongoConnection)
    val myRealm = environment.config.property("jwt.realm").getString()
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val formapagorepository = FormaPagoRepository()
    val categoryRepository = CategoryRepository()
    val invoiceRepository = InvoiceRepository()
    val lineasVentaRepository = LineasVentaRepository()
    val productRepository = ProductRepository()
    DatabaseFactory.init()

    install(Authentication) {
        jwt("jwt-auth") {
            realm = myRealm
            // Verifica que el token sea un token válido así como la signatura
            verifier(
                JWT.require(Algorithm.HMAC256(secret)).withAudience(audience).withIssuer(issuer).build()
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
                    HttpStatusCode.Unauthorized, "Token is not valid or has expired"
                )
            }
        }
    }

    routing {
        authenticate("jwt-auth") {
            get("/logged") {
                println("Logged User")
                call.respond(HttpStatusCode.OK)
            }
        }
        get("/products/category/{id_categoria}"){
            val id = call.parameters["id_categoria"] ?: ""
            if (id.isNotEmpty()) {
                val objectId = Integer.valueOf(id)
                val products = productRepository.getProductsByCategory(objectId)
                //var exist = userlist.find { u -> u._id == objectId }
                if (products.isNotEmpty()){
                    call.respond(HttpStatusCode.OK,products)
                }else{
                    call.respond(HttpStatusCode.NotFound)
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized, "There is no id on the message")
            }

        }
        post("/product") {
            try {
                val producto = call.receive<Producto>()
                val productos = productRepository.getAll()

                // Validación básica
                if (producto.nombre?.isBlank() == true) {
                    call.respond(HttpStatusCode.BadRequest, "El nombre de la categoría no puede estar vacío")
                    return@post
                }
                val exist = producto.nombre?.let { productRepository.getProductByName(it) }
                if (exist != null) {
                    call.respond(HttpStatusCode.Conflict, "Ese producto ya existe")
                    return@post
                }


                // Insertar y responder
                val inserted = productRepository.insertProduct(producto)
                call.respond(HttpStatusCode.Created, inserted)

            } catch (e: BadRequestException) {
                // Error al deserializar JSON
                call.respond(HttpStatusCode.BadRequest, "Formato JSON inválido: ${e.message}")

            } catch (e: DuplicateKeyException) {
                // Ejemplo si la categoría ya existe
                call.respond(HttpStatusCode.Conflict, "Ya existe un producto con ese nombre")

            } catch (e: Exception) {
                // Otros errores no esperados
                println(e)
                call.respond(HttpStatusCode.InternalServerError, "Error interno del servidor: ${e.message}")
            }
        }
        get("/") {
            val users = userRepository.getAll()
            //val usersResponses = mutableListOf<UserResponse>()
            val usersResponses = mutableListOf<User>()
            users.forEach {
                val userResponse = User(
                    id = it._id.toString(),
                    username = it.username,
                    password = it.password,
                    role = it.role,
                    name = it.name,
                )
                usersResponses.add(userResponse)
            }
            call.respond(
                HttpStatusCode.OK, usersResponses
            )
        }

        post("/auth/login") {
            val user = call.receive<User>()
            val userList = userRepository.getAll()
            val exist = userList.find { u -> u.username == user.username }
            if (exist == null) {
                call.respond(HttpStatusCode.Unauthorized, "User not found")
            } else {
                val result = BCrypt.verifyer().verify(user.password.toCharArray(), exist.password)

                if (result.verified) {
                    val token =
                        JWT.create().withAudience(audience).withIssuer(issuer).withClaim("username", user.username)
                            .withExpiresAt(Date(System.currentTimeMillis() + 3600000)).sign(Algorithm.HMAC256(secret))
                    call.respond(HttpStatusCode.OK, hashMapOf("token" to token))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Password invalid")
                }
            }
        }

        authenticate("jwt-auth") {
            post("/categories") {
                try {
                    val category = call.receive<Category>()
                    val categories = categoryRepository.getAll()

                    // Validación básica
                    if (category.nombre.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "El nombre de la categoría no puede estar vacío")
                        return@post
                    }
                    val exist = categoryRepository.getCategorieByName(category.nombre)
                    if (exist != null) {
                        call.respond(HttpStatusCode.Conflict, "Esa categoría ya existe")
                        return@post
                    }


                    // Insertar y responder
                    val inserted = categoryRepository.insertCategory(category)
                    call.respond(HttpStatusCode.Created, inserted)

                } catch (e: BadRequestException) {
                    // Error al deserializar JSON
                    call.respond(HttpStatusCode.BadRequest, "Formato JSON inválido: ${e.message}")

                } catch (e: DuplicateKeyException) {
                    // Ejemplo si la categoría ya existe
                    call.respond(HttpStatusCode.Conflict, "Ya existe una categoría con ese nombre")

                } catch (e: Exception) {
                    // Otros errores no esperados
                    println(e)
                    call.respond(HttpStatusCode.InternalServerError, "Error interno del servidor: ${e.message}")
                }
            }
        }


        get("/formspago") {
            val formasPago = formapagorepository.getAll()
            call.respond(formasPago)
        }
        get("/categories") {
            val categories = categoryRepository.getAll()
            call.respond(categories)
        }
        get("/invoices") {
            val invoices = invoiceRepository.getAll()
            call.respond(invoices)
        }
        get("/lineasventa") {
            val lineasVenta = lineasVentaRepository.getAll()
            call.respond(lineasVenta)
        }

        post("/formspago") {
            try {
                var forma = call.receive<Forma_pago>()
                println(forma)
                forma.nombre?.let { createFormaPago(it) }
                call.respond(HttpStatusCode.Created)
            } catch (e: IllegalStateException) {
                call.respond(
                    status = HttpStatusCode.BadRequest, message = mapOf("message" to e.message)
                )
            } catch (e: JsonConvertException) {
                call.respond(
                    status = HttpStatusCode.BadRequest, message = mapOf("message" to e.message)
                )
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest, message = mapOf("message" to e.message)
                )
            }
        }
        post("/category/delete/{category_id}") {
            val id = call.parameters["category_id"] ?: ""
            try {
                if (id.isNotEmpty()) {
                    val id_int= id.toInt()
                    val categories = categoryRepository.getAll()
                    var exist = categories.find { u -> u.id_category == id_int }
                    if (exist != null) {
                        categoryRepository.deleteById(id_int)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "There is no id on the message")
                }
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        post("/user/update/{id}") {
            val id = call.parameters["id"] ?: ""

        }
        authenticate("jwt-auth") {
            post("/user/delete/{id}") {
                val id = call.parameters["id"] ?: ""
                try {
                    if (id.isNotEmpty()) {
                        val objectId = ObjectId(id)
                        val userlist = userRepository.getAll()
                        var exist = userlist.find { u -> u._id == objectId }
                        if (exist != null) {
                            userRepository.removeById(objectId)
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "User not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "There is no id on the message")
                    }
                } catch (e: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }

        get("/pdf") {
            generateInvoicePDFBytes()
            call.respond(HttpStatusCode.OK)
        }

        authenticate("jwt-auth") {
            post("/users") {
                try {
                    var userExist = false
                    val userSer = call.receive<User>()
                    val bcryptHashPassword = BCrypt.withDefaults().hashToString(12, userSer.password.toCharArray())
                    val usersMongo = userRepository.getAll()
                    if (usersMongo != null) {
                        usersMongo.forEach {
                            if (it.username == userSer.username) {
                                userExist = true
                            }
                        }
                    }
                    if (!userExist) {
                        userSer.password = bcryptHashPassword
                        userRepository.add(userToUserBson(userSer))
                        call.respond(
                            HttpStatusCode.Created, "Usuario creado"
                        )
                    } else {
                        call.respond(HttpStatusCode.Conflict, "Usuario ya existente")
                    }

                } catch (e: IllegalStateException) {
                    call.respond(
                        status = HttpStatusCode.BadRequest, message = mapOf("message" to e.message)
                    )
                } catch (e: JsonConvertException) {
                    call.respond(
                        status = HttpStatusCode.BadRequest, message = mapOf("message" to e.message)
                    )
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.BadRequest, message = mapOf("message" to e.message)
                    )
                }
            }
        }

        staticResources("/static", "static")
    }
}

fun generateInvoicePDFBytes(): ByteArray {
    System.setProperty("sun.java2d.font.scaler", "true") // ayuda con el renderizado de fuentes
    val doc = PDDocument()
    val page = PDPage()
    doc.addPage(page)

    val content = PDPageContentStream(doc, page)
    val margin = 50f
    val startY = 750f
    var yPosition = startY

    // Encabezado
    content.beginText()
    content.setFont(PDType1Font.HELVETICA_BOLD, 20f)
    content.newLineAtOffset(margin, yPosition)
    content.showText("FACTURA")
    content.endText()

    yPosition -= 40f

    // Datos de la empresa
    content.beginText()
    content.setFont(PDType1Font.HELVETICA, 12f)
    content.newLineAtOffset(margin, yPosition)
    content.showText("Empresa XYZ S.A.")
    content.endText()

    yPosition -= 15f
    content.beginText()
    content.newLineAtOffset(margin, yPosition)
    content.showText("Dirección: Calle Falsa 123")
    content.endText()

    yPosition -= 30f

    // Datos de la factura
    content.beginText()
    content.setFont(PDType1Font.HELVETICA_BOLD, 14f)
    content.newLineAtOffset(margin, yPosition)
    content.showText("Número de Factura: 000123")
    content.endText()

    yPosition -= 20f

    content.beginText()
    content.setFont(PDType1Font.HELVETICA, 12f)
    content.newLineAtOffset(margin, yPosition)
    content.showText("Fecha: 2025-05-25")
    content.endText()

    yPosition -= 30f

    // Tabla de productos (simplificada)
    val tableHeaders = listOf("Cantidad", "Descripción", "Precio Unitario", "Total")
    val products = listOf(
        listOf("2", "Producto A", "10.00", "20.00"),
        listOf("1", "Producto B", "15.00", "15.00"),
        listOf("3", "Producto C", "7.50", "22.50"),
    )

    // Dibujar encabezados tabla
    content.beginText()
    content.setFont(PDType1Font.HELVETICA_BOLD, 12f)
    content.newLineAtOffset(margin, yPosition)
    content.showText(tableHeaders.joinToString("    "))
    content.endText()

    yPosition -= 20f

    // Dibujar productos
    content.setFont(PDType1Font.HELVETICA, 12f)
    for (product in products) {
        content.beginText()
        content.newLineAtOffset(margin, yPosition)
        content.showText(product.joinToString("    "))
        content.endText()
        yPosition -= 20f
    }

    yPosition -= 20f

    // Aquí dibujamos el QR justo después de la tabla de productos
    val qrSize = 100f
    val qrImage = generateQRCodeImage("https://tu-url-o-dato-qr.com", qrSize.toInt(), qrSize.toInt())
    val pdImage = LosslessFactory.createFromImage(doc, qrImage)
    val qrX = margin
    val qrY = yPosition - qrSize // colocamos el QR justo debajo del texto actual
    content.drawImage(pdImage, qrX, qrY, qrSize, qrSize)

    // Ajustamos yPosition para que el texto no se solape con el QR
    yPosition = qrY - 20f

    // Total final
    content.beginText()
    content.setFont(PDType1Font.HELVETICA_BOLD, 14f)
    content.newLineAtOffset(margin, yPosition)
    content.showText("Total: 57.50")
    content.endText()

    content.close()

    val outputStream = ByteArrayOutputStream()
    doc.save(outputStream)
    doc.close()
    File("factura.pdf").writeBytes(outputStream.toByteArray())
    return outputStream.toByteArray()
}
fun generateQRCodeImage(text: String, width: Int, height: Int): BufferedImage {
    val qrCodeWriter = QRCodeWriter()
    val qr="https://prewww2.aeat.es/wlpl/TIKE-CONT/ValidarQR?nif=20521995S&numserie=12345678/G33&fecha=01-01-2024&importe=241.4"
    val bitMatrix = qrCodeWriter.encode(qr, BarcodeFormat.QR_CODE, width, height)
    return MatrixToImageWriter.toBufferedImage(bitMatrix)
}
