package com.pluscubed.logcat.data

import java.io.File

class SendLogDetails {
    var subject: String? = null
    var body: String? = null
    var attachment: File? = null
    var attachmentType: AttachmentType? = null

    enum class AttachmentType(val mimeType: String) {
        None("text/plain"),
        Zip("application/zip"),
        Text("application/*")
    }
}
