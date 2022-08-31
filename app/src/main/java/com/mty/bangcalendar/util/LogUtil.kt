package com.mty.bangcalendar.util

import android.util.Log

object LogUtil {

    private const val VERBOSE = 1

    private const val DEBUG = 2

    private const val INFO = 3

    private const val WARN = 4

    private const val ERROR = 5

    private const val level = ERROR

    private const val DEFAULT_TAG = "LogUtil"

    fun v(tag: String, msg: String) {
        if (level <= VERBOSE) {
            Log.v(tag, msg)
        }
    }

    fun d(tag: String, msg: String) {
        if (level <= DEBUG) {
            Log.d(tag, msg)
        }
    }

    /**
     * 本函数将类名作为Log的Tag，只需传入msg即可
     * @param obj 一般情况下传入this即可，若this为匿名类，则采用LogUtil作为Tag
     */
    fun <T> d(obj: T, msg: String) {
        if (level <= DEBUG) {
            val tag = obj!!::class.simpleName
            if (tag != null) {
                Log.d(tag, msg)
            } else {
                Log.d(DEFAULT_TAG, msg)
            }
        }
    }

    fun i(tag: String, msg: String) {
        if (level <= INFO) {
            Log.i(tag, msg)
        }
    }

    fun w(tag: String, msg: String) {
        if (level <= WARN) {
            Log.w(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (level <= ERROR) {
            Log.e(tag, msg)
        }
    }

}