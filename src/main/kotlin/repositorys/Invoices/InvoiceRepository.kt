package com.example.repositorys.Category

import com.example.EntidadesMySQL.InvoicesMySQL
import com.example.dto.Invoice
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InvoiceRepository {
    fun getAll(): List<Invoice> {
        return transaction {
            InvoicesMySQL.selectAll().map {
                Invoice(
                    idInvoice = it[InvoicesMySQL.id],
                    numSerie = it[InvoicesMySQL.numSerie],
                    fechaEmision = it[InvoicesMySQL.fechaEmision],
                    baseImponible = it[InvoicesMySQL.baseImponible],
                    totalIva = it[InvoicesMySQL.totalIva],
                    total = it[InvoicesMySQL.total],
                    estado = it[InvoicesMySQL.estado],
                    firmaHash = it[InvoicesMySQL.firmaHash],
                    hashAnterior = it[InvoicesMySQL.hashAnterior],
                    algoritmoCifrado = it[InvoicesMySQL.algoritmoCifrado],
                    operador = it[InvoicesMySQL.operador],
                    formaPago = it[InvoicesMySQL.formaPagoId]
                )
            }
        }
    }

    fun getById(id: Int): Invoice? {
        return transaction {
            InvoicesMySQL.select { InvoicesMySQL.id eq id }
                .map {
                    Invoice(
                        idInvoice = it[InvoicesMySQL.id],
                        numSerie = it[InvoicesMySQL.numSerie],
                        fechaEmision = it[InvoicesMySQL.fechaEmision],
                        baseImponible = it[InvoicesMySQL.baseImponible],
                        totalIva = it[InvoicesMySQL.totalIva],
                        total = it[InvoicesMySQL.total],
                        estado = it[InvoicesMySQL.estado],
                        firmaHash = it[InvoicesMySQL.firmaHash],
                        hashAnterior = it[InvoicesMySQL.hashAnterior],
                        algoritmoCifrado = it[InvoicesMySQL.algoritmoCifrado],
                        operador = it[InvoicesMySQL.operador],
                        formaPago = it[InvoicesMySQL.formaPagoId]
                    )
                }
                .singleOrNull()
        }
    }

    fun insertInvoice(invoice: Invoice): Int? {
        val invoices=getAll()
        val previous= invoices[invoices.size-1]

        val huella=generarHuellaSHA256(
            idEmisor = "20521995S",
            numSerie = invoice.numSerie.toString(),
            fechaExpedicion = invoice.fechaEmision.toString(),
            tipoFactura = "F2",
            cuotaTotal = invoice.totalIva.toString(),
            importeTotal = invoice.total.toString(),
            huellaAnterior = previous.firmaHash.toString(),
            fechaHoraGenRegistro = invoice.fechaEmision.toString(),
        )
        invoice.firmaHash=huella.uppercase()
        invoice.hashAnterior=previous.firmaHash.toString()
        val localDate = invoice.fechaEmision?.toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val localDateFormated = localDate?.format(formatter) ?: ""
        val localDateFormatedPrevious = previous.fechaEmision?.format(formatter) ?: ""
        val xmlBase="""
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
           xmlns:sum="https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd"
           xmlns:sum1="https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd"
           xmlns:xd="http://www.w3.org/2000/09/xmldsig#">
           <soapenv:Header/>
           <soapenv:Body>
              <sum:RegFactuSistemaFacturacion>
                 <sum:Cabecera>
                    <sum1:ObligadoEmision>
                       <sum1:NombreRazon>FriendlyTPV</sum1:NombreRazon>
                       <sum1:NIF>20521995S</sum1:NIF>
                    </sum1:ObligadoEmision>
                 </sum:Cabecera>
                 <sum:RegistroFactura>
                    <sum1:RegistroAlta>
                       <sum1:IDVersion>1.0</sum1:IDVersion>
                       <sum1:IDFactura>
                          <sum1:IDEmisorFactura>20521995S</sum1:IDEmisorFactura>
                          <sum1:NumSerieFactura>${invoice.numSerie}</sum1:NumSerieFactura>
                          <sum1:FechaExpedicionFactura>${localDateFormated}</sum1:FechaExpedicionFactura>
                       </sum1:IDFactura>
                       <sum1:NombreRazonEmisor>FriendlyTPV</sum1:NombreRazonEmisor>
                       <sum1:TipoFactura>F2</sum1:TipoFactura>
                       <sum1:DescripcionOperacion>Ticket</sum1:DescripcionOperacion>
                       <sum1:FacturaSinIdentifDestinatarioArt61d>S</sum1:FacturaSinIdentifDestinatarioArt61d>
                       <sum1:Desglose>
                          <sum1:DetalleDesglose>
                             <sum1:ClaveRegimen>01</sum1:ClaveRegimen>
                             <sum1:CalificacionOperacion>S1</sum1:CalificacionOperacion>
                             <sum1:TipoImpositivo>21</sum1:TipoImpositivo>
                             <sum1:BaseImponible0importeNoSujeto>${invoice.baseImponible}</sum1:BaseImponible0importeNoSujeto>
                             <sum1:CuotaRepercutida>${invoice.totalIva}</sum1:CuotaRepercutida>
                          </sum1:DetalleDesglose>
                       </sum1:Desglose>
                       <sum1:CuotaTotal>${invoice.totalIva}</sum1:CuotaTotal>
                       <sum1:ImporteTotal>${(invoice.baseImponible?.let { invoice.totalIva?.plus(it) })}</sum1:ImporteTotal>
                       <sum1:Encadenamiento>
                          <sum1:RegistroAnterior>
                             <sum1:IDEmisorFactura>20521995S</sum1:IDEmisorFactura>
                             <sum1:NumSerieFactura>${previous.numSerie}</sum1:NumSerieFactura>
                             <sum1:FechaExpedicionFactura>$localDateFormatedPrevious</sum1:FechaExpedicionFactura>
                             <sum1:Huella>${invoice.hashAnterior}</sum1:Huella>
                          </sum1:RegistroAnterior>
                       </sum1:Encadenamiento>
                       <sum1:SistemaInformatico>
                          <sum1:NombreRazon>FriendlyTPV</sum1:NombreRazon>
                          <sum1:NIF>20521995S</sum1:NIF>
                          <sum1:NombreSistemaInformatico>FeriendlyTPV</sum1:NombreSistemaInformatico>
                          <sum1:IdSistemaInformatico>1</sum1:IdSistemaInformatico>
                          <sum1:Version>0.1</sum1:Version>
                          <sum1:NumeroInstalacion>1</sum1:NumeroInstalacion>
                          <sum1:TipoUsoPosibleSoloVerifactu>N</sum1:TipoUsoPosibleSoloVerifactu>
                          <sum1:TipoUsoPosibleMultiOT>S</sum1:TipoUsoPosibleMultiOT>
                          <sum1:IndicadorMultiples0T>S</sum1:IndicadorMultiples0T>
                       </sum1:SistemaInformatico>
                       <sum1:FechaHoraHusoGenRegistro>${LocalDateTime.now()}</sum1:FechaHoraHusoGenRegistro>
                       <sum1:TipoHuella>01</sum1:TipoHuella>
                       <sum1:Huella>${invoice.firmaHash}</sum1:Huella>
                    </sum1:RegistroAlta>
                 </sum:RegistroFactura>
              </sum:RegFactuSistemaFacturacion>
           </soapenv:Body>
        </soapenv:Envelope> 
    """.trimIndent()
        val file = File("RegistroFacturacion.xml")
        file.writeText(xmlBase)
        return transaction {
            InvoicesMySQL.insert {
                if (invoice.numSerie != null) it[numSerie] = invoice.numSerie
                if (invoice.fechaEmision != null) it[fechaEmision] = invoice.fechaEmision
                if (invoice.baseImponible != null) it[baseImponible] = invoice.baseImponible
                if (invoice.totalIva != null) it[totalIva] = invoice.totalIva
                if (invoice.total != null) it[total] = invoice.total
                if (invoice.estado != null) it[estado] = invoice.estado
                if (invoice.firmaHash != null) it[firmaHash] = invoice.firmaHash
                if (invoice.hashAnterior != null) it[hashAnterior] = invoice.hashAnterior
                if (invoice.algoritmoCifrado != null) it[algoritmoCifrado] = invoice.algoritmoCifrado
                if (invoice.operador != null) it[operador] = invoice.operador
                if (invoice.formaPago != null) it[formaPagoId] = invoice.formaPago
            }.getOrNull(InvoicesMySQL.id)
        }

    }
    fun generarHuellaSHA256(
        idEmisor: String,
        numSerie: String,
        fechaExpedicion: String,
        tipoFactura: String,
        cuotaTotal: String,
        importeTotal: String,
        huellaAnterior: String,
        fechaHoraGenRegistro: String
    ): String {
        val camposConcatenados = listOf(
            idEmisor,
            numSerie,
            fechaExpedicion,
            tipoFactura,
            cuotaTotal,
            importeTotal,
            huellaAnterior,
            fechaHoraGenRegistro
        ).joinToString("")

        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(camposConcatenados.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) } // Hexadecimal en minúsculas
    }
}