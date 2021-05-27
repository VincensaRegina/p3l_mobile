package com.vincensaregina.p3lproject.cameraQRNope

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.ChecksumException
import com.google.zxing.FormatException
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import java.nio.ByteBuffer
import android.graphics.ImageFormat.YUV_420_888
import android.graphics.ImageFormat.YUV_422_888
import android.graphics.ImageFormat.YUV_444_888

//to inspect individual frames from the camera retrieved using CameraX and analyse those frames using the ZXing library to attempt to locate a QR Code.
//To use this class for image analysis using CameraX it will need to implement the ImageAnalysis.Analyzer interface from CameraX
//and override the analyze(…) method.
class QRCodeImageAnalyzer(private val listener: QRCodeFoundListener) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        //check the image frame from the camera is in a compatible format.
        // If this is the case then we will retrieve the image from the CameraX buffer and convert it into a byte array.
        if (image.format == YUV_420_888 || image.format == YUV_422_888 || image.format == YUV_444_888) {
            val byteBuffer: ByteBuffer = image.planes[0].buffer
            val imageData = ByteArray(byteBuffer.capacity())
            byteBuffer.get(imageData)
            val source = PlanarYUVLuminanceSource(
                imageData,
                image.width, image.height,
                0, 0,
                image.width, image.height,
                false
            )
            //convert the byte array into a format usable by the ZXing library to scan for QR codes
            //which will require us to create a PlanarYUVLuminanceSource object then pass it to
            //a HybridBinarizer object to generate a BinaryBitmap.
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            try {
                //If a QR code can be located in the BinaryBitmap using the QR code reader library from ZXing library
                // we will invoke the onQRCodeFound(…) method on the QRCodeFoundListener listener
                // while passing the text from the QR code as a parameter.
                val result: Result = QRCodeMultiReader().decode(binaryBitmap)
                listener.onQRCodeFound(result.text)
            } catch (e: FormatException) {
                listener.qrCodeNotFound()
            } catch (e: ChecksumException) {
                listener.qrCodeNotFound()
            } catch (e: NotFoundException) {
                listener.qrCodeNotFound()
            }
        }
        image.close()
    }
}