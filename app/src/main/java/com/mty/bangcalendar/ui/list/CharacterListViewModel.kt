package com.mty.bangcalendar.ui.list

import androidx.lifecycle.*
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Character
import kotlinx.coroutines.launch

class CharacterListViewModel : ViewModel() {

    private val _characterList = MutableLiveData<List<Character>>()
    val characterList: LiveData<List<Character>>
        get() = _characterList
    fun getCharacterList() {
        viewModelScope.launch {
            _characterList.value = Repository.getCharacterList()
        }
    }
}