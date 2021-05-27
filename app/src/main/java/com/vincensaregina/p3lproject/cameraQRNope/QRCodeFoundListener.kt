package com.vincensaregina.p3lproject.cameraQRNope

interface QRCodeFoundListener {
    // create a listener we will use in the custom image analyzer class we will create to inform the Activity
    // when a QR code has been located in an image frame OR when a QR code could not be located.
    fun onQRCodeFound(qrCode: String)
    fun qrCodeNotFound()
}