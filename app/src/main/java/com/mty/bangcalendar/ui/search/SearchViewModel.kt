package com.mty.bangcalendar.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Event

class SearchViewModel : ViewModel() {
    val glideOptions = RequestOptions()
        .skipMemoryCache(false)
        .diskCacheStrategy(DiskCacheStrategy.ALL)

    private val searchEventLiveData = MutableLiveData<Int>()
    val event: LiveData<Event?> = Transformations.switchMap(searchEventLiveData) {
        Repository.getEventById(it)
    }
    fun getEventById(id: Int) {
        searchEventLiveData.value = id
    }

    private val searchCharacterLiveData = MutableLiveData<String>()
    val character = Transformations.switchMap(searchCharacterLiveData) {
        Repository.getCharacterByName(it)
    }
    fun getCharacterByName(name: String) {
        searchCharacterLiveData.value = name
    }
}