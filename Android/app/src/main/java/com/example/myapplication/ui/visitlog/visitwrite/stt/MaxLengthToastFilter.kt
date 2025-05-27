package com.example.myapplication.ui.visitlog.visitwrite.stt

import android.text.InputFilter
import android.text.Spanned

class MaxLengthToastFilter(
    private val maxLen: Int,
    private val onExceeded: () -> Unit      // 토스트 등 알림 콜백
) : InputFilter {

    override fun filter(
        source: CharSequence?,  // 새로 입력된 문자열
        start: Int,
        end: Int,
        dest: Spanned?,         // 기존 문자열
        dstart: Int,
        dend: Int
    ): CharSequence? {

        val keep = maxLen - (dest?.length ?: 0) + (dend - dstart)  // 남은 자리 수

        return when {
            keep <= 0 -> {          // 한 글자도 못 붙임
                onExceeded()        // 알림
                ""                  // 아무것도 추가 안 함
            }

            keep >= end - start -> {
                null                // 전부 허용 (기존 LengthFilter 동작)
            }

            else -> {               // 일부만 허용
                onExceeded()
                source?.subSequence(start, start + keep)  // 남은 만큼만 잘라 붙임
            }
        }
    }
}
