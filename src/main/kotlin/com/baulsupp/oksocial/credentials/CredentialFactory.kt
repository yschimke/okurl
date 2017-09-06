package com.baulsupp.oksocial.credentials

import com.mcdermottroe.apple.OSXKeychainException
import com.baulsupp.oksocial.output.util.PlatformUtil
import java.util.Optional

object CredentialFactory {
    @Throws(OSXKeychainException::class)
    fun createCredentialsStore(tokenSet: Optional<String>): CredentialsStore {
        return if (PlatformUtil.isOSX()) {
            OSXCredentialsStore(tokenSet)
        } else {
            PreferencesCredentialsStore(tokenSet)
        }
    }
}
