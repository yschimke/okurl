package com.baulsupp.oksocial.process

import okio.ByteString
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.nio.charset.StandardCharsets

data class ExecResult(val exitCode: Int, val output: ByteString) {
  val success = exitCode == 0
  fun outputString(): String = output.string(StandardCharsets.UTF_8)
}

suspend fun exec(vararg command: String): ExecResult {
  return exec(command.toList())
}

suspend fun exec(command: List<String>, configure: ProcessExecutor.() -> Unit = {}): ExecResult {
  val pe = ProcessExecutor().command(command.toList())
    .readOutput(true)
    .redirectError(Slf4jStream.ofCaller().asInfo())

  configure(pe)

  return run {
    val pr = pe.execute()
    val output = pr.output()
    ExecResult(pr.exitValue, ByteString.of(output, 0, output.size))
  }
}