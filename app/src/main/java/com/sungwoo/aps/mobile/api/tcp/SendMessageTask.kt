package com.sungwoo.aps.mobile.api.tcp

import android.os.AsyncTask
import android.util.Log

/**
 * Created by localadmin on 8/17/2017.
 */
class SendMessageTask internal constructor(private val mTcpClient: TcpClient, private val msg: String) : AsyncTask<String?, String, String>() {
    companion object {
        val TAG = "SendMessageTask"
    }

//    private lateinit var mTcpClient: TcpClient
//    private lateinit var msg: String

//    constructor(mTcpClient: TcpClient,msg:String) : this() {
//        this.mTcpClient = mTcpClient
//        this.msg = msg
//    }

    override fun doInBackground(vararg messages: String?): String {
        Log.i(TAG, messages.get(0))
        mTcpClient.sendMessage(msg)
        return ""
    }

}