package com.example.notesapp.Database

import androidx.room.*

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE uid = :uid LIMIT 1")
    suspend fun getProfile(uid: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profile WHERE uid = :uid")
    suspend fun deleteProfile(uid: String)
}