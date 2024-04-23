package com.orbys.quizz.domain.models

import com.orbys.quizz.R
import kotlinx.coroutines.flow.MutableStateFlow

// Clase que representa una pregunta.
data class Question(
    var question: String,
    val answerType: AnswerType = AnswerType.NONE,
    var answers: MutableStateFlow<List<Answer>> = MutableStateFlow(emptyList()),
    val maxNumericAnswer: Int = 1,
    val icon: Int = R.drawable.ic_help,
    val isAnonymous: Boolean = true,
    val isMultipleAnswers: Boolean = false,
    val isMultipleChoices: Boolean = false,
    val timeOut: Int? = null,
) {
    // Id autonumerico de la pregunta.
    val id: Int = nextId++

    companion object {
        private var nextId = 0
    }
}