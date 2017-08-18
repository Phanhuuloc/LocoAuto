package com.sungwoo.aps.mobile.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.sungwoo.aps.mobile.R
import android.os.AsyncTask
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.sungwoo.aps.mobile.adapters.ClientListAdapter
import com.sungwoo.aps.mobile.api.tcp.StopClientTask
import com.sungwoo.aps.mobile.api.tcp.TcpClient
import java.security.AccessControlContext
import java.util.*


class ClientActivity : AppCompatActivity() {

    private var mList: ListView? = null
    private var arrayList: ArrayList<String>? = null
    private var mAdapter: ClientListAdapter? = null
    private var mTcpClient: TcpClient? = null

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, ClientActivity::class.java))
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        arrayList = ArrayList<String>()

        val editText = findViewById(R.id.editText) as EditText
        val send = findViewById(R.id.send_button) as Button

        //relate the listView from java to the one created in xml
        mList = findViewById(R.id.list) as ListView
        mAdapter = ClientListAdapter(this, arrayList!!)
        mList!!.setAdapter(mAdapter)

        send.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {

                val message = editText.text.toString()

                //add the text in the arrayList
                arrayList!!.add("c: " + message)

                //sends the message to the server
                if (mTcpClient != null) {
//                    mTcpClient!!.sendMessage(message)
                    var task = SendMessageTask(message)
                    task.execute()
                }

                //refresh the list
                mAdapter!!.notifyDataSetChanged()
                editText.setText("")
            }
        })

    }

    override fun onPause() {
        super.onPause()

        // disconnect
        StopClientTask(mTcpClient).execute()
//        mTcpClient!!.stopClient()
        mTcpClient = null

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        if (mTcpClient != null) {
            // if the client is connected, enable the connect button and disable the disconnect one
            menu.getItem(1).setEnabled(true)
            menu.getItem(0).setEnabled(false)
        } else {
            // if the client is disconnected, enable the disconnect button and disable the connect one
            menu.getItem(1).setEnabled(false)
            menu.getItem(0).setEnabled(true)
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.getItemId()) {
            R.id.connect -> {
                // connect to the server
                ConnectTask().execute("")
                return true
            }
            R.id.disconnect -> {
                // disconnect
//                mTcpClient!!.stopClient()
                StopClientTask(mTcpClient).execute()

                // clear the data set
                arrayList!!.clear()
                // notify the adapter that the data set has changed.
                mAdapter!!.notifyDataSetChanged()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    inner class ConnectTask : AsyncTask<String, String, TcpClient>() {

        override fun doInBackground(vararg message: String): TcpClient? {

            //we create a TCPClient object and
            mTcpClient = TcpClient(object : TcpClient.OnMessageReceived {
                //here the messageReceived method is implemented
                override fun messageReceived(message: String) {
                    //this method calls the onProgressUpdate
                    publishProgress(message)
                }
            })
            mTcpClient!!.run()

            return null
        }

        override fun onProgressUpdate(vararg values: String) {
            super.onProgressUpdate(*values)

            //in the arrayList we add the messaged received from server
            arrayList!!.add(values[0])
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter!!.notifyDataSetChanged()
        }
    }

    inner class SendMessageTask internal constructor(val msg:String): AsyncTask<String?, String, String>() {
        val TAG = "SendMessageTask"


//        private lateinit var mTcpClient: TcpClient
//        private lateinit var msg: String
//
//        constructor(mTcpClient: TcpClient,msg:String) : this() {
//            this.mTcpClient = mTcpClient
//            this.msg = msg
//        }

        override fun doInBackground(vararg messages: String?): String {
            Log.i(TAG, messages.get(0))
            mTcpClient?.sendMessage(msg)
            return ""
        }

    }

}
