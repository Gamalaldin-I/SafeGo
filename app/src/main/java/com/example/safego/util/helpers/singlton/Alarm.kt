package com.example.safego.util.helpers.singlton

import android.content.Context
import android.media.MediaPlayer
import com.example.safego.R

object Alarm {
        private var mediaPlayer: MediaPlayer? = null

        fun playAlarm(context: Context) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.emergency_alarm)
                mediaPlayer?.isLooping = true // لو عايز الإنذار يفضل شغال لحد ما توقفه
            }
            mediaPlayer?.start()
        }

        fun stopAlarm() {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

