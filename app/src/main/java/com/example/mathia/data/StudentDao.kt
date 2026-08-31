package com.example.mathia.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Query("SELECT * FROM t_estudiante WHERE studentId = :studentId LIMIT 1")
    fun getStudentFlow(studentId: String): Flow<StudentEntity?>

    @Query("SELECT * FROM t_estudiante WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: String): StudentEntity?

    @Query("SELECT * FROM t_estudiante LIMIT 1")
    suspend fun getStudentProfile(): StudentEntity?

    @Query("DELETE FROM t_estudiante")
    suspend fun clearAll()
}