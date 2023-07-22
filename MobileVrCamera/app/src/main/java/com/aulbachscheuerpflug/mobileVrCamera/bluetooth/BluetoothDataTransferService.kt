package com.aulbachscheuerpflug.mobileVrCamera.bluetooth

import android.bluetooth.BluetoothSocket
import com.aulbachscheuerpflug.mobileVrCamera.ACCUMULATED_DATA_SEPARATION_CHAR
import com.aulbachscheuerpflug.mobileVrCamera.BLUETOOTH_TRANSFER_SIZE
import com.aulbachscheuerpflug.mobileVrCamera.FAILED_INCOMING_DATA
import com.aulbachscheuerpflug.mobileVrCamera.utils.showDebugInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothDataTransferService(
    private val socket: BluetoothSocket,
    private val progressCallback: (Int) -> Unit
) {
    fun listenForIncomingMessages(): Flow<Message> {
        return flow {
            if (!socket.isConnected) {
                return@flow
            }

            val buffer = ByteArray(BLUETOOTH_TRANSFER_SIZE)
            val accumulatedData = StringBuilder()
            while (true) {
                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (error: IOException) {
                    throw IOException(FAILED_INCOMING_DATA)
                }

                accumulatedData.append(buffer.decodeToString(endIndex = byteCount))

                if (accumulatedData.endsWith(ACCUMULATED_DATA_SEPARATION_CHAR)) {
                    val message =
                        accumulatedData.removeSuffix(ACCUMULATED_DATA_SEPARATION_CHAR).toString()
                    if (message.startsWith("false") || message.startsWith("true")) {
                        emit(message.toSettingsMessage())
                    } else {
                        emit(message.toBluetoothMessage())
                    }
                    accumulatedData.clear()
                }
            }
        }.flowOn(Dispatchers.IO)
    }


    suspend fun sendMessage(byteArray: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            showDebugInfo(byteArray.size.toString())
            val startTime = System.currentTimeMillis()
            try {
                val chunks = byteArray.toList().chunked(BLUETOOTH_TRANSFER_SIZE)
                for ((index, chunk) in chunks.withIndex()) {
                    socket.outputStream.write(chunk.toByteArray())
                    progressCallback((index + 1) * 100 / chunks.size)
                }
            } catch (error: IOException) {
                return@withContext false
            }
            val endTime = System.currentTimeMillis()
            val timeTaken = endTime - startTime
            showDebugInfo("Transfer took $timeTaken milliseconds")
            true
        }
    }
}