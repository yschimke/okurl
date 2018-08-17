#!/usr/bin/env okscript

import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.kotlin.*
import com.baulsupp.okurl.services.cronhub.ping
import kotlinx.coroutines.runBlocking

if (args.isEmpty()) usage("must supply uuid")

runBlocking { ping(args[0]) }
