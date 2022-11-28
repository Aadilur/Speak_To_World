package com.translator.app.speak.to.world.speaktoworld.view_model

import androidx.lifecycle.*
import com.translator.app.speak.to.world.speaktoworld.repository.RepoAppList

class ViewModelAppList(private val repo: RepoAppList = RepoAppList()): ViewModel() {
    fun getResponseUsingLiveData() = repo.getResponseFromFirestoreUsingLiveData()

    private var _otpCode: MutableLiveData<String> = MutableLiveData("")
    var otpCode: MutableLiveData<String> = MutableLiveData("")


}