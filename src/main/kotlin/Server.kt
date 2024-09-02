package machankura.vsock.example

import machankura.vsockk.vsock.VSockAddress
import machankura.vsockk.vsock.ServerVSock
import machankura.vsockk.vsock.VSock
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

fun main() {
    //VSockImpl().load()  //We should find load or initialization BaseVsock must handle it
    val vsockServer = VSockServer()
    try {
        vsockServer.start(VSockAddress(VSockAddress.VMADDR_CID_ANY, 9000)) //use a constant CID like 4 (in Nitro enclave environment) instead of VSockAddress.VMADDR_CID_ANY
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

class VSockServer {

    private var serverSocket: ServerVSock? = null
    private val bufferSize = 4096

    fun start(address: VSockAddress) {
        serverSocket = ServerVSock()
        try {
            serverSocket?.bind(address)
            println("VSock Server listening on CID: ${serverSocket?.localCid}")

            serverSocket?.accept()?.use { peerVSock: VSock ->
                val buffer = ByteArray(bufferSize)
                val bytesRead = peerVSock.inputStream?.read(buffer, 0, bufferSize)
                if (bytesRead != null && bytesRead > 0) {
                    val receivedData = java.lang.String(buffer, 0, bytesRead, StandardCharsets.UTF_8).trim()
                    println("Received Data: $receivedData")

                    // Check if received data is valid JSON
                    val isValidJson = isValidJson(receivedData)

                    val responseMessage = if (isValidJson) {
                        """{"response": "Valid JSON received"}"""
                    } else {
                        """{"error": "Invalid JSON format"}"""
                    }

                    peerVSock.outputStream?.write(responseMessage.toByteArray(StandardCharsets.UTF_8))
                }
            }
        } catch (ex: IOException) {
            println("Error starting VSock Server: ${ex.message}")
        } finally {
            try {
                serverSocket?.close()
            } catch (closeEx: IOException) {
                println("Error closing VSock Server: ${closeEx.message}")
            }
        }
    }

    // Function to validate if a string is a valid JSON
    private fun isValidJson(jsonString: String): Boolean {
        return try {
            Json.decodeFromJsonElement<JsonObject>(Json.parseToJsonElement(jsonString))
            true
        } catch (e: Exception) {
            false
        }
    }
}