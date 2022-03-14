package com.ckc.renote

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException

data class Recording(var times: MutableList<Long>, var states: MutableList<State>)

class Rewinder {
    private var audioRecorder: MediaRecorder? = null
    private var audioPlayer: MediaPlayer? = null
    private lateinit var recording: Recording
    private var initialTime: Long = 0
    private var isRecording: Boolean = false

    fun startRecording(state: State, audioFile: String) {
        audioRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(null, "prepare() failed")
            }

            start()
        }

        recording = Recording(mutableListOf(), mutableListOf())
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
    }

    fun playRecording(editor: Editor, saved: State) {
        editor.setEditable(false)
        editor.setContent(recording.states[0].content)
        for (i in 1 until recording.states.size) {
            Handler(Looper.getMainLooper()).postDelayed({
                editor.setContent(recording.states[i].content)
            }, recording.times[i - 1])
        }
        Handler(Looper.getMainLooper()).postDelayed({
            editor.setContent(saved.content)
            editor.setEditable(true)
        }, recording.times.last())
    }
}
