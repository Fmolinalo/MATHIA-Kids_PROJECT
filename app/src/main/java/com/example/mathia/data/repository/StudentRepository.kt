package com.example.mathia.data.repository

import com.example.mathia.FirebaseRepository
import com.example.mathia.data.StudentDao
import com.example.mathia.data.mapper.ModelMappers.toDomain
import com.example.mathia.data.mapper.ModelMappers.toEntity
import com.example.mathia.data.mapper.ModelMappers.toFirebase
import com.example.mathia.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(
    private val studentDao: StudentDao,
    private val firebaseRepository: FirebaseRepository
) {

    fun getStudentFlow(studentId: String): Flow<Student?> {
        return studentDao.getStudentFlow(studentId).map { entity ->
            entity?.toDomain()
        }
    }

    suspend fun saveStudentLocal(student: Student) {
        studentDao.insertStudent(student.toEntity())
    }

    suspend fun syncStudentWithRemote(studentId: String): Student? {
        return try {
            val remoteFirebaseStudent = firebaseRepository.obtenerAlumnoPorPin(studentId)
            if (remoteFirebaseStudent != null) {
                val domainStudent = remoteFirebaseStudent.toDomain(studentId)
                studentDao.insertStudent(domainStudent.toEntity())
                domainStudent
            } else {
                val localEntity = studentDao.getStudentById(studentId)
                localEntity?.toDomain()
            }
        } catch (e: Exception) {
            val localEntity = studentDao.getStudentById(studentId)
            localEntity?.toDomain()
        }
    }

    suspend fun pushStudentUpdate(student: Student): Boolean {
        saveStudentLocal(student)
        return try {
            val firebaseStudent = student.toFirebase()
            firebaseRepository.crearAlumno(
                alumno = mapOf(
                    "nombre" to firebaseStudent.nombre,
                    "apodo" to firebaseStudent.apodo,
                    "grado" to firebaseStudent.grado,
                    "nivel_actual" to firebaseStudent.nivel_actual,
                    "estrellas" to firebaseStudent.estrellas,
                    "xp" to firebaseStudent.xp,
                    "streak" to firebaseStudent.streak,
                    "precision" to firebaseStudent.precision,
                    "avatar" to firebaseStudent.avatar,
                    "equipped_theme" to firebaseStudent.equipped_theme
                ),
                pin = student.pin.ifEmpty { student.id.toString() }
            )
        } catch (e: Exception) {
            false
        }
    }
}
