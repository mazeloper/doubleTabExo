package com.stacon.doubletabplayerview.log

import android.util.Log


object JsLog {

    private const val TAG = "Exo"

    /**
     * 디버그 모드 여부
     *
     * @return
     */
    private fun isDebug(): Boolean {
        return true
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 로그미출력 : 0, 모든로그출력 : 5
     */
    private const val LOG_VALUE = 5

    /**
     * BLACK 1
     */
    const val mVerbose = 1

    /**
     * BLUE 2
     */
    const val mDebug = 2

    /**
     * GREEN 3
     */
    const val mInformation = 3

    /**
     * YELLOW 4
     */
    const val mWarning = 4

    /**
     * RED 5
     */
    const val mError = 5


    /**
     * BLACK 1
     */
    fun verbose(message: String?) {
        if (isDebug()) {
            if (mVerbose <= LOG_VALUE) {
                print(mVerbose, StringBuilder(getLog()).append("] ").append(message).toString())
            }
        }
    }

    /**
     * BLUE 2
     */
    fun debug(message: String?) {
        if (isDebug()) {
            if (mDebug <= LOG_VALUE) {
                print(mDebug, StringBuilder(getLog()).append("] ").append(message).toString())
            }
        }
    }

    /**
     * GREEN 3
     */
    fun information(message: String?) {
        if (isDebug()) {
            if (mInformation <= LOG_VALUE) {
                print(mInformation, StringBuilder(getLog()).append("] ").append(message).toString())
            }
        }
    }

    /**
     * YELLOW 4
     */
    fun warning(message: String?) {
        if (isDebug()) {
            if (mWarning <= LOG_VALUE) {
                print(mWarning, StringBuilder(getLog()).append("] ").append(message).toString())
            }
        }
    }

    /**
     * RED 5
     */
    fun error(message: String?) {
        if (isDebug()) {
            if (mWarning <= LOG_VALUE) {
                print(mError, StringBuilder(getLog()).append("] ").append(message).toString())
            }
        }
    }

    /**
     * RED 5
     */
    fun error(message: String, exception: Exception?) {
        if (isDebug()) {
            if (mError <= LOG_VALUE) {
                print(
                    mError,
                    StringBuilder(getLog()).append("] ").append(message + exception.toString())
                        .toString()
                )
            } else {
                exception?.printStackTrace()
            }
        } else {
            // Do nothing
        }
        exception?.printStackTrace()
    }

    private fun print(level: Int, log: String) {
        when (level) {
            mVerbose -> Log.v(TAG, log)
            mDebug -> Log.d(TAG, log)
            mInformation -> Log.i(TAG, log)
            mWarning -> Log.w(TAG, log)
            mError -> Log.e(TAG, log)
            else -> {
            }
        }
    }

    private fun getLog(): String {
        return StringBuilder(getClassName()).append("::").append(getMethodName()).append(" [")
            .append(getLineNumber()).toString()
    }

    private fun getClassName(): String {
        var result: String
        try {
            val stackTraceElement = Thread.currentThread().stackTrace[LOG_VALUE]
            val className = stackTraceElement.className
            result = className.substring(className.lastIndexOf(".") + 1, className.length)
        } catch (e: Exception) {
            result = "{\$CLASS}"
            e.printStackTrace()
        }
        return result
    }

    private fun getMethodName(): String {
        var result: String
        try {
            val stackTraceElement = Thread.currentThread().stackTrace[LOG_VALUE]
            result = stackTraceElement.methodName
        } catch (e: Exception) {
            result = "{\$METHOD}"
            e.printStackTrace()
        }
        return result
    }

    private fun getLineNumber(): String {
        var result = ""
        try {
            val stackTraceElement = Thread.currentThread().stackTrace[LOG_VALUE]
            result = stackTraceElement.lineNumber.toString()
        } catch (e: Exception) {
            result = "{\$LINE_NUMBER}"
            e.printStackTrace()
        }
        return result
    }

}