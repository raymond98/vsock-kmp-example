package machankura.vsock.example

import machankura.vsockk.vsock.VSock
import machankura.vsockk.vsock.VSockAddress
import java.io.IOException
import java.nio.charset.StandardCharsets

fun main() {
    val vsockClient = VSockClient(VSockAddress(4, 9000))
    try {
        val message = """{"greeting": "Hello, from the VSockClient!"}"""
        vsockClient.sendMessage(message)
        val response = vsockClient.receiveMessage()
        println("Received from server: $response")
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        vsockClient.close()
    }
}

class VSockClient(address: VSockAddress) {

    private val sock: VSock = VSock(address)

    init {
        sock.connect(address)  // Ensure the VSock is connected
    }

    fun sendMessage(message: String) {
        val dataToSend = message.toByteArray(StandardCharsets.UTF_8)
        sock.outputStream?.use { output ->
            output.write(dataToSend)
            output.flush()
        } ?: throw IOException("Output stream is not available")
    }

    fun receiveMessage(): String {
        val buffer = ByteArray(4096)
        val bytesRead = sock.inputStream?.use { input ->
            input.read(buffer, 0, buffer.size)
        } ?: throw IOException("Input stream is not available")

        if (bytesRead > 0) {
            return String(buffer, 0, bytesRead, StandardCharsets.UTF_8).trim()
        } else {
            throw IOException("No data read from the input stream")
        }
    }

    fun close() {
        sock.close()
    }
}
