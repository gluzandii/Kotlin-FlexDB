package org.solo.kotlin.flexdb

import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import org.solo.kotlin.flexdb.db.DB


object GlobalData {
    @JvmStatic
    var db: DB? = null

    @JvmStatic
    var handle: KeysetHandle
        private set

    init {
        AeadConfig.register()
        handle = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
    }
}