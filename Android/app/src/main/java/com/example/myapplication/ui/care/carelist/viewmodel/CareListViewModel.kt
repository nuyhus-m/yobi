package com.example.myapplication.ui.care.carelist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.care.carelist.CareListFragment
import com.example.myapplication.ui.care.carelist.data.CareUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CareListViewModel @Inject constructor() : ViewModel() {

    private val _careUserList = MutableLiveData<List<CareUser>>()
    val careUserList: LiveData<List<CareUser>> = _careUserList

    init {
        _careUserList.value = listOf(
            CareUser(R.drawable.ic_profile, "박진현", "여성", "2000/12/20"),
            CareUser(R.drawable.ic_profile, "이서현", "여성", "2000/12/20"),
            CareUser(R.drawable.ic_profile, "이문경", "여성", "2000/12/20"),
            CareUser(R.drawable.ic_profile, "차현우", "여성", "2000/12/20"),
            CareUser(R.drawable.ic_profile, "이호정", "여성", "2000/12/20"),
            CareUser(R.drawable.ic_profile, "두식이", "여성", "2000/12/20"),
        )
    }
}