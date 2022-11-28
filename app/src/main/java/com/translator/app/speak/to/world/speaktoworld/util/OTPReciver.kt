package com.translator.app.speak.to.world.speaktoworld.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.translator.app.speak.to.world.speaktoworld.view_model.ViewModelAppList
import java.util.regex.Pattern


//    *********** Pass ViewModel as parameter with default value as ViewModel **************

//class OTPReceiver(val viewModel: ViewModelAppList = ViewModelAppList()) : BroadcastReceiver() {
//
//
//    //****************************************************************************
//    //****************************************************************************
//
//    // Application Hash for Auto Sms API is: +o7p5cVQ8rw
//
//    //****************************************************************************
//    //****************************************************************************
//
//
////    ************************** Inside onCreate in Desire Activity ************************
////
////    val client = SmsRetriever.getClient(this /* context */)
////    val task: Task<Void> = client.startSmsRetriever()
////    task.addOnSuccessListener {}
////    task.addOnFailureListener {}
////
////    val otpReceiver= OTPReceiver(viewModelAppList)
////    val filter = IntentFilter()
////    filter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
////    registerReceiver(otpReceiver,filter)
//
//    override fun onReceive(context: Context?, intent: Intent?) {
//
//        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
//            val extras = intent.extras
//
//            if (extras != null) {
//                val status = extras[SmsRetriever.EXTRA_STATUS] as Status
//                var timedOut = false
//                var otpCode: String? = null
//
//                when (status.statusCode) {
//                    CommonStatusCodes.SUCCESS -> {
//
//                        val sms = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?
//                        sms?.let {
//                            // val p = Pattern.compile("[0-9]+") check a pattern with only digit
//                            val p = Pattern.compile("\\d+")
//                            val m = p.matcher(it)
//                            if (m.find()) {
//                                val otp = m.group()
//
//                                viewModel.otpCode.value = otp
//
//                            }
//                        }
//
//
//                    }
//                    CommonStatusCodes.TIMEOUT -> {
//                        timedOut = true
//                    }
//                }
//
//
//            }
//        }
//    }
//
//}
