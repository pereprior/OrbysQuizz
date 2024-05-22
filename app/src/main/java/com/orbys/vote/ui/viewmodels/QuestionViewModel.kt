package com.orbys.vote.ui.viewmodels

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orbys.vote.core.managers.NetworkManager
import com.orbys.vote.domain.models.Question
import com.orbys.vote.domain.usecases.AddQuestionUseCase
import com.orbys.vote.domain.usecases.GetQuestionUseCase
import com.orbys.vote.domain.usecases.GetServerUrlUseCase
import com.orbys.vote.domain.usecases.ModifyFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que proporciona funciones para interactuar con los casos de uso de la lista de preguntas.
 */
@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val getQuestionUseCase: GetQuestionUseCase,
    private val addQuestionUseCase: AddQuestionUseCase,
    private val getSeverUrlUseCase: GetServerUrlUseCase,
    private val modifyFileUseCase: ModifyFileUseCase,
    private val networkManager: NetworkManager
): ViewModel() {
    fun getQuestion() = getQuestionUseCase()
    fun addQuestion(question: Question) { addQuestionUseCase(question) }
    fun getServerUrl(endpoint: String, isHotspot: Boolean = false) = getSeverUrlUseCase(endpoint, isHotspot)
    fun modifyFile(lineNumber: Int, content: String) {
        viewModelScope.launch { modifyFileUseCase(lineNumber, content) }
    }

    fun isNetworkAvailable(activity: AppCompatActivity) = networkManager.isNetworkAvailable(activity)
    fun checkNetworkOnActivity(activity: AppCompatActivity) {
        viewModelScope.launch { networkManager.checkNetworkOnActivity(activity) }
    }
}