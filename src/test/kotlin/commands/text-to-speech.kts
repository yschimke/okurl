#!/usr/bin/env okscript

// Step 1
// Install okurl
// $ brew install yschimke/tap/okurl

// Step 2
// Confirm your account is working here, get oauth2 credentials etc, enable cloud APIs
// https://developers.google.com/apis-explorer/?hl=en_US#p/texttospeech/v1beta1/texttospeech.text.synthesize

// Step 3
// Authorise Google Access
// $ okurl --authorize google
// Use scopes: https://www.googleapis.com/auth/cloud-platform,plus.login,plus.profile.emails.read

// Step 4 say stuff
// $ ./text-to-speech.kts 'Hello, how are you?'

import com.baulsupp.okurl.kotlin.*
import com.baulsupp.oksocial.output.SimpleResponse
import kotlinx.coroutines.experimental.runBlocking
import okio.ByteString.Companion.decodeBase64

enum class SsmlVoiceGender {
  SSML_VOICE_GENDER_UNSPECIFIED, MALE, FEMALE, NEUTRAL
}

enum class AudioEncoding {
  AUDIO_ENCODING_UNSPECIFIED, LINEAR16, MP3, OGG_OPUS
}

data class SynthesisInput(
  val text: String? = null,
  val ssml: String? = null
)

data class VoiceSelectionParams(
  val languageCode: String,
  val name: String? = null,
  val ssmlGender: SsmlVoiceGender? = null
)

data class AudioConfig(
  val audioEncoding: AudioEncoding,
  val speakingRate: Double? = 1.0,
  val pitch: Double? = 0.0,
  val volumeGainDb: Double? = 0.0,
  val sampleRateHertz: Double? = null
)

data class TextToSpeechRequest(
  val input: SynthesisInput,
  val voice: VoiceSelectionParams,
  val audioConfig: AudioConfig
)

data class TextToSpeechResponse(val audioContent: String) {
  fun audio() = audioContent.decodeBase64()!!
}

val text = args.joinToString(" ")

println("Saying '$text'")

val req = TextToSpeechRequest(
  SynthesisInput(text),
  VoiceSelectionParams("en-gb", "en-GB-Standard-A", SsmlVoiceGender.FEMALE),
  AudioConfig(AudioEncoding.LINEAR16)
)

runBlocking {
  val speechRequest = request("https://texttospeech.googleapis.com/v1beta1/text:synthesize") {
    postJsonBody(req)
  }
  val response = client.query<TextToSpeechResponse>(speechRequest)

  simpleOutput.playAudio(SimpleResponse("audio/wav", response.audio()))
}
