package com.baulsupp.oksocial.kotlin

import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.function.BiConsumer
import kotlin.coroutines.experimental.Continuation

suspend fun Call.await(): Response {
  return kotlinx.coroutines.experimental.suspendCancellableCoroutine { c ->
    enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        c.resumeWithException(e)
      }

      override fun onResponse(call: Call, response: Response) {
        c.resume(response)
      }
    })
  }
}

private class ContinuationConsumer<T>(
        @Volatile @JvmField var cont: Continuation<T>?
) : BiConsumer<T?, Throwable?> {
  @Suppress("UNCHECKED_CAST")
  override fun accept(result: T?, exception: Throwable?) {
    val cont = this.cont ?: return // atomically read current value unless null
    if (exception == null) // the future has been completed normally
      cont.resume(result as T)
    else // the future has completed with an exception
      cont.resumeWithException(exception)
  }
}

suspend fun <T> CompletableFuture<T>.await(): T {
  // fast path when CompletableFuture is already done (does not suspend)
  if (isDone) {
    try {
      return get()
    } catch (e: ExecutionException) {
      throw e.cause ?: e // unwrap original cause from ExecutionException
    }
  }
  // slow path -- suspend
  return suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
    val consumer = ContinuationConsumer(cont)
    whenComplete(consumer)
    cont.invokeOnCompletion {
      consumer.cont = null // shall clear reference to continuation
    }
  }
}