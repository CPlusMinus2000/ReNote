package com.ckc.renote

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import kotlinx.serialization.Serializable
import java.io.File
import java.io.IOException

@Serializable
data class Recording(var audio: String, var times: MutableList<Long>, var states: MutableList<State>)

class Rewinder {
    private var audioRecorder: MediaRecorder? = null
    private var audioPlayer: MediaPlayer? = null
    var recording: Recording? = null
    private lateinit var tempFile: String
    private var initialTime: Long = 0
    private var isRecording: Boolean = false

    fun setTempFile(file: String) {
        tempFile = file
    }

    fun startRecording(state: State) {
        audioRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(tempFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(null, "recording failed")
            }
        }

        recording = Recording("", mutableListOf(), mutableListOf())
        initialTime = System.currentTimeMillis()
        recording!!.states.add(state)
        isRecording = true
    }

    fun addState(state: State) {
        if (isRecording) {
            val timeStamp = System.currentTimeMillis()
            recording!!.times.add(timeStamp - initialTime)
            recording!!.states.add(state)
        }
    }

    fun stopRecording() {
        recording!!.times.add(System.currentTimeMillis() - initialTime)
        isRecording = false
        audioRecorder?.apply {
            stop()
            release()
        }
        audioRecorder = null

        val temp = File(tempFile)
        recording!!.audio = Base64.encodeToString(temp.readBytes(), Base64.DEFAULT)
        temp.delete()
    }

    fun playRecording(editor: Editor, saved: State) {
        if (recording != null) {
            val temprec = recording!!
            editor.setEditable(false)
            editor.setContent(temprec.states[0].content)
            val temp = File(tempFile)
            temp.writeBytes(Base64.decode(temprec.audio, Base64.DEFAULT))
            audioPlayer = MediaPlayer().apply {
                try {
                    setDataSource(tempFile)
                    prepare()
                    start()
                } catch (e: IOException) {
                    Log.e(null, "playback failed")
                }
            }
            for (i in 1 until temprec.states.size) {
                Handler(Looper.getMainLooper()).postDelayed({
                    editor.setContent(temprec.states[i].content)
                }, temprec.times[i - 1])
            }
            Handler(Looper.getMainLooper()).postDelayed({
                audioPlayer?.release()
                audioPlayer = null
                temp.delete()
                editor.setContent(saved.content)
                editor.setEditable(true)
            }, temprec.times.last())
        }
    }
}
