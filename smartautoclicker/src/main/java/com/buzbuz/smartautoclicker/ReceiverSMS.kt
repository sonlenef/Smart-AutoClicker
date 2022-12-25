package com.buzbuz.smartautoclicker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage


/**
 * Create by SonLe on 22/12/2022
 */
class ReceiverSMS: BroadcastReceiver() {
    override fun onReceive(p0: Context?, intent: Intent?) {
        if (intent?.action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            val extras = intent!!.extras

            var strMessage = ""

            if (extras != null) {
                val smsextras = extras["pdus"] as Array<Any>?
                for (i in smsextras!!.indices) {
                    val smsmsg = SmsMessage.createFromPdu(smsextras[i] as ByteArray)
                    val strMsgBody = smsmsg.messageBody.toString()
                    val strMsgSrc = smsmsg.originatingAddress
                    strMessage += "SMS from $strMsgSrc : $strMsgBody"
                }
            }
            println()
        }
    }
}