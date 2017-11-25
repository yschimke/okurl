package com.baulsupp.oksocial.location

class BestLocation : LocationSource {
  override fun read(): Location? = FileLocationSource(FileLocationSource.FILE).read() ?: CoreLocationCLI().read()
}
