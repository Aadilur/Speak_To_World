package com.translator.app.speak.to.world.speaktoworld.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.translator.app.speak.to.world.speaktoworld.data.DataAppList
import com.translator.app.speak.to.world.speaktoworld.data.ResponseAppList

class RepoAppList(
    private val rootRef: DatabaseReference = FirebaseDatabase.getInstance().reference,
    private val productRef: DatabaseReference = rootRef.child("AppList")
) {
    fun getResponseFromFirestoreUsingLiveData(): MutableLiveData<ResponseAppList> {
        val mutableLiveData = MutableLiveData<ResponseAppList>()
        var dataAppList: DataAppList
        val responseAppList = ResponseAppList()
        responseAppList.items = ArrayList()

//        productRef.get().addOnCompleteListener { it ->
//
//            if (it.isSuccessful) {
//
//                try {
//
//                    responseAppList.items!!.clear()
//                    it.result.children.forEach {
//                        dataAppList = it.getValue(DataAppList::class.java)!!
//                        responseAppList.items?.add(dataAppList)
//                    }
//
//                    Log.d("data1", "result called")
//                }catch (exception:Exception){
//                    responseAppList.exception = it.exception
//                }
//
//
//            }else {
//                responseAppList.exception = it.exception
//            }
//
//            mutableLiveData.value = responseAppList
//        }

        productRef.orderByChild("imagePriority").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    responseAppList.items!!.clear()
                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            dataAppList = dataSnapshot.getValue(DataAppList::class.java)!!
                            responseAppList.items?.add(dataAppList)
                        }
                        Log.d("data1", "data get called.....")
                    }
                } catch (e: Exception) {
                    responseAppList.exception = e
                }
                mutableLiveData.value = responseAppList

            }

            override fun onCancelled(error: DatabaseError) {
                responseAppList.error = error
            }
        })
        return mutableLiveData
    }
}




