package com.pprior.quizz.ui.components.recyclerview

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.pprior.quizz.R
import com.pprior.quizz.data.flow.FlowRepository
import com.pprior.quizz.databinding.FragmentListItemBinding
import com.pprior.quizz.domain.models.Question
import com.pprior.quizz.ui.activities.dialogs.EditQuestionActivity
import com.pprior.quizz.ui.activities.dialogs.LaunchQuestionActivity
import com.pprior.quizz.ui.components.alert.ConfirmDialog
import org.koin.java.KoinJavaComponent.inject

/**
 * Vista para la tarjeta de cada pregunta.
 *
 * @property binding El vinculo para la vista de la pregunta.
 */
class QuestionViewHolder(
    private val binding: FragmentListItemBinding
): RecyclerView.ViewHolder(binding.root) {

    private val repository: FlowRepository by inject(FlowRepository::class.java)

    fun bind(question: Question) = with(binding) {
        // Vincula una pregunta a la vista
        questionTypeIcon.setImageResource(question.icon)
        titleQuestion.text = question.question

        deleteButton.setOnClickListener {
            deleteQuestion(it.context, question)
        }

        // Muestra el dialogo para poder contestar esa pregunta
        launchButton.setOnClickListener {
            showLaunchQuestionDialog(question, it)
        }

        // Muestra el diálogo para editar la pregunta
        editButton.setOnClickListener {
            showEditQuestionDialog(question, it)
        }
    }

    private fun deleteQuestion(
        context: Context,
        question: Question
    ) {
        ConfirmDialog(context, message = context.getString(R.string.delete_question_message)) {
            repository.deleteQuestion(question)
        }
    }

    private fun showLaunchQuestionDialog(question: Question, view: View) {
        val intent = Intent(view.context, LaunchQuestionActivity::class.java)
        intent.putExtra("question", question.question)
        view.context.startActivity(intent)
    }

    private fun showEditQuestionDialog(question: Question, view: View) {
        val intent = Intent(view.context, EditQuestionActivity::class.java)
        intent.putExtra("questionQuestion", question.question)
        view.context.startActivity(intent)
    }

}