package com.likelion.liontalk.core.util

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.FX_FOCUS_NAVIGATION_DOWN
import android.media.AudioManager.FX_FOCUS_NAVIGATION_UP
import android.media.AudioManager.FX_KEYPRESS_RETURN
import android.media.AudioManager.FX_KEY_CLICK

/**
 * 시스템 사운드 효과를 재생하는 유틸 클래스입니다.
 *
 * 앱에서 [SoundType]에 따라 등록된 효과음을 재생합니다.
 */
class SoundPlayer(private val context: Context) {

    private val soundMap: Map<SoundType, Int> = mapOf(
        SoundType.MESSAGE_SENT to FX_KEY_CLICK,
        SoundType.MESSAGE_RECEIVE to FX_KEYPRESS_RETURN,
        SoundType.ENTER_ROOM to FX_FOCUS_NAVIGATION_UP,
        SoundType.LEAVE_ROOM to FX_FOCUS_NAVIGATION_DOWN,
    )

    /**
     * 전달받은 [type]에 해당하는 시스템 사운드 효과를 재생합니다.
     */
    fun play(type: SoundType) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        soundMap.get(type)?.let { audioManager.playSoundEffect(it)}
    }

}
