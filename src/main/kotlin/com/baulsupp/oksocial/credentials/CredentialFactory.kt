package com.baulsupp.oksocial.credentials

import com.baulsupp.oksocial.output.util.PlatformUtil
import com.mcdermottroe.apple.OSXKeychainException
import java.util.*

object CredentialFactory {
    @Throws(OSXKeychainException::class)
    fun createCredentialsStore(tokenSet: String?): CredentialsStore {
        return if (PlatformUtil.isOSX()) {
            OSXCredentialsStore(tokenSet)
        } else {
            PreferencesCredentialsStore(tokenSet)
        }
    }
}
