package com.baulsupp.oksocial.services.microsoft.model

import com.squareup.moshi.Json

data class User(
  val businessPhones: List<String>?,
  val preferredLanguage: String?,
  val mail: String?,
  val mobilePhone: String?,
  val officeLocation: String?,
  val displayName: String,
  val surname: String?,
  val givenName: String?,
  val jobTitle: String?,
  val id: String,
  val userPrincipalName: String?
)

data class Token(val access_token: String, val refresh_token: String, val scope: String, val token_type: String, val expires_in: Int)

data class LastModifiedBy(val application: Application, val user: User)
data class ParentReference(val path: String, val driveId: String, val driveType: String, val id: String)
data class CreatedBy(val application: Application, val user: User)
data class FileSystemInfo(val lastModifiedDateTime: String, val createdDateTime: String)
data class Folder(val view: View, val childCount: Int)
data class SpecialFolder(val name: String)
data class Application(val displayName: String , val id: String)
data class View(val sortOrder: String , val viewType: String , val sortBy: String)
data class DriveItem(
  val lastModifiedDateTime: String,
  val lastModifiedBy: LastModifiedBy?,
  val createdDateTime: String,
  val parentReference: ParentReference?,
  val folder: Folder?,
  val size: Int,
  val createdBy: CreatedBy,
  val webUrl: String,
  val name: String,
  val cTag: String?,
  val eTag: String?,
  val id: String,
  val specialFolder: SpecialFolder?,
  val fileSystemInfo: FileSystemInfo?
)
data class DriveRootList(@Json(name = "@odata.context") val OdataContext: String, val value: List<DriveItem>)
