package com.sungwoo.aps.mobile.api.tcp

import android.os.AsyncTask

/**
 * Created by localadmin on 8/17/2017.
 */
class StopClientTask(): AsyncTask<String?, String, Void>() {
    private var mTcpClient: TcpClient? = null

    constructor(mTcpClient: TcpClient?) : this() {
        this.mTcpClient = mTcpClient
    }

    override fun doInBackground(vararg params: String?): Void {
        mTcpClient!!.stopClient()
        mTcpClient = null
        return null as Void
    }
}