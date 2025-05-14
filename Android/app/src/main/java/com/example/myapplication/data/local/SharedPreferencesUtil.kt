package com.example.myapplication.data.local

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesUtil (context: Context) {

    val SHARED_PREFERENCES_NAME = "yobi_preference"

    var preferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String) {
        preferences.edit().apply {
            putString("accessToken", accessToken)
            putString("refreshToken", refreshToken)
            apply()
        }
    }

    fun getAccessToken(): String? {
        return preferences.getString("accessToken", null)
    }

    fun getRefreshToken(): String? {
        return preferences.getString("refreshToken", null)
    }

    fun clearTokens() {
        preferences.edit().apply {
            remove("accessToken")
            remove("refreshToken")
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }


//    //사용자 정보 저장
//    fun addUser(user: User){
//        preferences.edit() {
//            putString("id", user.id)
//            putString("name", user.name)
//        }
//    }
//    //사용자 정보 가져오기
//    fun getUser(): User {
//        val id = preferences.getString("id", "")
//        if (id != ""){
//            val name = preferences.getString("name", "")
//            return User(id!!, name!!, "",0)
//        }else{
//            return User()
//        }
//    }
//    //사용자 정보 삭제
//    fun deleteUser(){
//        //preference 지우기
//        preferences.edit() {
//            clear()
//        }
//    }

}