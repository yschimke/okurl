#!/usr/bin/env okscript

import com.baulsupp.okurl.kotlin.usage
import com.baulsupp.okurl.services.cronhub.ping
import kotlinx.coroutines.runBlocking

if (args.isEmpty()) usage("must supply uuid")

runBlocking { ping(args[0]) }
