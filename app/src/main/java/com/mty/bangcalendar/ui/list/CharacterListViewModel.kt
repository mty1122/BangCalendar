package com.mty.bangcalendar.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Character

class CharacterListViewModel : ViewModel() {

    private val getCharacterListLiveData = MutableLiveData<Any?>()
    val characterList: LiveData<List<Character>> = Transformations.switchMap(getCharacterListLiveData) {
        Repository.getCharacterList()
    }
    fun getCharacterList() {
        getCharacterListLiveData.value = getCharacterListLiveData.value
    }
}