package com.orbys.quizz.domain.usecases

import com.orbys.quizz.domain.models.Question
import com.orbys.quizz.domain.repositories.IQuestionRepository
import javax.inject.Inject

class AddQuestionUseCase @Inject constructor(
    private val repository: IQuestionRepository
) {
    operator fun invoke(question: Question) { repository.addQuestion(question) }
}