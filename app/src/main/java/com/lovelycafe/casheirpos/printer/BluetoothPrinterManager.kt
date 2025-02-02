package com.lovelycafe.casheirpos.printer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.*

class BluetoothPrinterManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var bluetoothSocket: BluetoothSocket? = null
    private var isConnected: Boolean = false

    companion object {
        private val UUID_PRINTER = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard UUID for serial devices
    }

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun findPrinter(printerName: String): BluetoothDevice? {
        if (!hasBluetoothPermissions()) {
            throw SecurityException("Missing Bluetooth permissions")
        }

        return try {
            bluetoothAdapter?.bondedDevices?.find { it.name == printerName }
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun connectToPrinter(device: BluetoothDevice): Boolean {
        return withContext(Dispatchers.IO) {
            if (!hasBluetoothPermissions()) {
                throw SecurityException("Missing Bluetooth permissions")
            }
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_PRINTER)
                bluetoothSocket?.connect()
                isConnected = true
                true
            } catch (e: SecurityException) {
                e.printStackTrace()
                false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    internal fun textToImage(text: String, textSize: Float = 24f): Bitmap {
        val paint = Paint().apply {
            color = Color.BLACK
            this.textSize = textSize
            typeface = Typeface.create("Nyala", Typeface.NORMAL)  // Use Ethiopian font
            flags = Paint.ANTI_ALIAS_FLAG
        }

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        val width = 384  // Standard thermal printer width
        val height = bounds.height()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawText(text, 0f, -bounds.top.toFloat(), paint)

        return bitmap
    }

    private fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val bytesPerLine = (width + 7) / 8
        val result = ByteArray(bytesPerLine * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = pixels[y * width + x]
                val luminance = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                if (luminance < 150) {  // Dark pixel
                    val byteIndex = y * bytesPerLine + x / 8
                    val bitIndex = 7 - (x % 8)
                    result[byteIndex] = (result[byteIndex].toInt() or (1 shl bitIndex)).toByte()
                }
            }
        }
        return result
    }

    suspend fun printAmharicText(text: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (!isConnected) {
                throw IllegalStateException("Not connected to a printer")
            }
            if (!hasBluetoothPermissions()) {
                throw SecurityException("Missing Bluetooth permissions")
            }

            try {
                val outputStream: OutputStream = bluetoothSocket?.outputStream
                    ?: return@withContext false

                // Initialize printer
                outputStream.write(byteArrayOf(0x1B, 0x33, 0x00))

                // Convert text to image
                val bitmap = textToImage(text)
                val imageData = bitmapToBytes(bitmap)

                // Print bitmap command
                val width = bitmap.width
                val height = bitmap.height
                val bytesPerLine = (width + 7) / 8

                outputStream.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00))
                outputStream.write(bytesPerLine)
                outputStream.write(0)
                outputStream.write(height)
                outputStream.write(0)

                // Send the image data
                outputStream.write(imageData)

                // Feed and cut
                outputStream.write(byteArrayOf(0x0A))
                outputStream.flush()
                true
            } catch (e: SecurityException) {
                e.printStackTrace()
                false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun printBitmap(bitmap: Bitmap): Boolean {
        return withContext(Dispatchers.IO) {
            if (!isConnected) {
                throw IllegalStateException("Not connected to a printer")
            }
            if (!hasBluetoothPermissions()) {
                throw SecurityException("Missing Bluetooth permissions")
            }

            try {
                val outputStream: OutputStream = bluetoothSocket?.outputStream
                    ?: return@withContext false

                // Initialize printer
                outputStream.write(byteArrayOf(0x1B, 0x33, 0x00))

                // Convert bitmap to bytes
                val imageData = bitmapToBytes(bitmap)

                // Print bitmap command
                val width = bitmap.width
                val height = bitmap.height
                val bytesPerLine = (width + 7) / 8

                outputStream.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00))
                outputStream.write(bytesPerLine)
                outputStream.write(0)
                outputStream.write(height)
                outputStream.write(0)

                // Send the image data
                outputStream.write(imageData)

                // Feed and cut
                outputStream.write(byteArrayOf(0x0A))
                outputStream.flush()
                true
            } catch (e: SecurityException) {
                e.printStackTrace()
                false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun printData(data: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (!isConnected) {
                throw IllegalStateException("Not connected to a printer")
            }
            if (!hasBluetoothPermissions()) {
                throw SecurityException("Missing Bluetooth permissions")
            }

            try {
                val outputStream: OutputStream = bluetoothSocket?.outputStream
                    ?: return@withContext false

                // Convert string data to bytes
                val bytes = data.toByteArray(Charsets.UTF_8)

                // Send the data to the printer
                outputStream.write(bytes)
                outputStream.flush()
                true
            } catch (e: SecurityException) {
                e.printStackTrace()
                false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun disconnect() {
        try {
            bluetoothSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bluetoothSocket = null
            isConnected = false
        }
    }
}
