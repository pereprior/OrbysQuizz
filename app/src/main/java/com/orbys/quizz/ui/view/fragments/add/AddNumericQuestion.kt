package com.orbys.quizz.ui.view.fragments.add

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.orbys.quizz.R
import com.orbys.quizz.domain.models.AnswerType
import com.orbys.quizz.domain.models.Question
import com.orbys.quizz.ui.components.managers.AnswerFieldsManager

/**
 * Clase que representa una actividad para añadir preguntas de tipo "Numerico".
 */
class AddNumericQuestion: AddFragment() {

    companion object {
        private const val MAX_DIGITS = 4
        private const val MIN_ANSWERS = 1
        private const val MAX_ANSWERS = 1
    }

    private lateinit var fieldsManager: AnswerFieldsManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            fieldsManager = AnswerFieldsManager(
                context = requireContext(),
                layout = answersLayout,
                hintText = getString(R.string.numeric_answer_hint),
                maxLength = MAX_DIGITS,
                minAnswers = MIN_ANSWERS,
                maxAnswers = MAX_ANSWERS,
                numericAnswer = true
            )

            val title: TextView? = activity?.findViewById(R.id.title)
            title?.text = "${getString(R.string.numeric_question_type_title)} ${getString(R.string.question_question_hint)}"
            questionTypeIcon.setImageResource(R.drawable.ic_number)

            multiAnswerTitle.visibility = View.GONE
            multiAnswerGroup.visibility = View.GONE
            multiAnswerDivider.visibility = View.GONE
        }

        fieldsManager.addAnswerField()
    }

    override fun createQuestionFromInput() = Question(
        question = binding.questionQuestion.text.toString(),
        icon = R.drawable.ic_number,
        answerType = AnswerType.NUMERIC,
        maxNumericAnswer = fieldsManager.getAnswersText().firstOrNull()?.toIntOrNull() ?: 0,
        isAnonymous = binding.anonymousQuestionOption.isChecked,
        timeOut = binding.timeoutInput.text.toString().toIntOrNull() ?: 0,
        isMultipleAnswers = binding.nonFilterUsersQuestionOption.isChecked
    )

    override fun saveQuestion(context: Context) {
        if (fieldsManager.anyAnswerIsEmpty()) {
            errorMessage(R.string.empty_answers_error)
            return
        }

        super.saveQuestion(context)
    }

}