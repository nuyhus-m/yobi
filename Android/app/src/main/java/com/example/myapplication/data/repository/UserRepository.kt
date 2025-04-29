package com.example.myapplication.data.repository

import com.example.myapplication.data.remote.UserService
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userService: UserService
) {

}