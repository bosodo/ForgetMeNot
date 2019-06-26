package com.odnovolov.forgetmenot.domain.repository

import com.odnovolov.forgetmenot.domain.entity.ExerciseCard
import com.odnovolov.forgetmenot.domain.entity.ExerciseData
import io.reactivex.Observable

interface ExerciseRepository {
    // Create
    fun saveExercise(exerciseData: ExerciseData)

    // Read
    fun observeExercise(): Observable<ExerciseData>

    // Update
    fun updateExerciseCard(exerciseCard: ExerciseCard)

    // Delete
    fun deleteAllExercises()
}