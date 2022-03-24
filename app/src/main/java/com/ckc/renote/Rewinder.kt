package com.ckc.renote

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.serialization.Serializable
import java.io.File
import java.io.IOException

@Serializable
data class Recording(var audio: ByteArray, var times: MutableList<Long>, var states: MutableList<State>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Recording

        if (!audio.contentEquals(other.audio)) return false
        if (times != other.times) return false
        if (states != other.states) return false

        return true
    }

    override fun hashCode(): Int {
        var result = audio.contentHashCode()
        result = 31 * result + times.hashCode()
        result = 31 * result + states.hashCode()
        return result
    }
}

class Rewinder {
    private var audioRecorder: MediaRecorder? = null
    private var audioPlayer: MediaPlayer? = null
    private lateinit var recording: Recording
    private lateinit var tempFile: String
    private var initialTime: Long = 0
    private var isRecording: Boolean = false

    fun startRecording(state: State, audioFile: String) {
        audioRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            tempFile = audioFile

            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(null, "recording failed")
            }
        }

        recording = Recording(ByteArray(1), mutableListOf(), mutableListOf())
        initialTime = System.currentTimeMillis()
        recording.states.add(state)
        isRecording = true
    }

    fun addState(state: State) {
        if (isRecording) {
            val timeStamp = System.currentTimeMillis()
            recording.times.add(timeStamp - initialTime)
            recording.states.add(state)
        }
    }

    fun stopRecording() {
        recording.times.add(System.currentTimeMillis() - initialTime)
        isRecording = false
        audioRecorder?.apply {
            stop()
            release()
        }
        audioRecorder = null

        val temp = File(tempFile)
        recording.audio = temp.readBytes()
        temp.delete()
    }

    fun playRecording(editor: Editor, saved: State) {
        editor.setEditable(false)
        editor.setContent(recording.states[0].content)
        val temp = File(tempFile)
        temp.writeBytes(recording.audio)
        audioPlayer = MediaPlayer().apply {
            try {
                setDataSource(tempFile)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(null, "playback failed")
            }
        }
        for (i in 1 until recording.states.size) {
            Handler(Looper.getMainLooper()).postDelayed({
                editor.setContent(recording.states[i].content)
            }, recording.times[i - 1])
        }
        Handler(Looper.getMainLooper()).postDelayed({
            audioPlayer?.release()
            audioPlayer = null
            temp.delete()
            editor.setContent(saved.content)
            editor.setEditable(true)
        }, recording.times.last())
    }
}
