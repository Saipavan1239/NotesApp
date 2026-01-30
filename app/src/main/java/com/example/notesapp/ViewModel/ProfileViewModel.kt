package com.example.notesapp.ViewModel


import androidx.lifecycle.*
import com.example.notesapp.Database.UserProfileEntity
import com.example.notesapp.Repository.UserProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: UserProfileRepository
) : ViewModel() {


    private val _profileImage = MutableLiveData<UserProfileEntity?>()
    val profileImage: LiveData<UserProfileEntity?> = _profileImage

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _about = MutableLiveData<String>()
    val about: LiveData<String> = _about

    /* ---------- LOAD PROFILE ---------- */

    fun loadProfile(uid: String) {
        viewModelScope.launch {
            _profileImage.value = repository.getLocalProfile(uid)
        }

        repository.getUserDetails(uid) { name, about ->
            _name.postValue(name ?: "Your Name")
            _about.postValue(about ?: "Tap to add about")
        }
    }

    /* ---------- IMAGE ---------- */

    fun saveProfileImage(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.saveLocalProfile(profile)
            _profileImage.value = profile
        }
    }

    fun removeProfileImage(uid: String) {
        viewModelScope.launch {
            repository.deleteLocalProfile(uid)
            _profileImage.value = null
        }
    }

    /* ---------- NAME & ABOUT ---------- */

    fun updateName(uid: String, newName: String) {
        repository.updateName(uid, newName)
        _name.value = newName
    }

    fun updateAbout(uid: String, newAbout: String) {
        repository.addNewAbout(uid, newAbout)
        _about.value = newAbout
    }

}
