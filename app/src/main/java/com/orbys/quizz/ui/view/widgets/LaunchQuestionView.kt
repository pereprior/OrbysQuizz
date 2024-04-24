package com.orbys.quizz.ui.view.widgets

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.orbys.quizz.R
import com.orbys.quizz.data.utils.ServerUtils
import com.orbys.quizz.data.utils.ServerUtils.Companion.QUESTION_ENDPOINT
import com.orbys.quizz.databinding.ServiceLaunchQuestionBinding
import com.orbys.quizz.domain.models.Bar
import com.orbys.quizz.domain.models.Question
import com.orbys.quizz.domain.repositories.QuestionRepositoryImpl
import com.orbys.quizz.domain.repositories.UsersRepositoryImpl
import com.orbys.quizz.ui.components.QRCodeGenerator
import com.orbys.quizz.ui.view.activities.DownloadActivity
import com.orbys.quizz.ui.view.activities.MainActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Clase que representa una vista para lanzar una pregunta a ser contestada.
 *
 * @property binding Objeto de enlace para acceder a los elementos de la interfaz de usuario.
 * @property windowManager Gestor de ventanas para controlar la vista.
 * @property layoutParams Parámetros de diseño de la ventana.
 * @property questionRepository Repositorio para gestionar las operaciones relacionadas con las preguntas.
 * @property usersRepository Repositorio para gestionar las operaciones relacionadas con los usuarios.
 * @property x Coordenada x de la vista.
 * @property y Coordenada y de la vista.
 * @property onChangeX Cambio en la coordenada x durante un evento de movimiento.
 * @property onChangeY Cambio en la coordenada y durante un evento de movimiento.
 */
class LaunchQuestionView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
): ConstraintLayout(context, attrs, defStyleAttr), View.OnTouchListener {

    private var binding: ServiceLaunchQuestionBinding
    private var windowManager: WindowManager
    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        },
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    )

    private val questionRepository = QuestionRepositoryImpl.getInstance()
    private val usersRepository = UsersRepositoryImpl.getInstance()
    private val serverUtils = ServerUtils()

    private var x: Int = 0
    private var y: Int = 0
    private var onChangeX: Float = 0f
    private var onChangeY: Float = 0f

    init {
        binding = ServiceLaunchQuestionBinding.inflate(LayoutInflater.from(context))
        bindOnQuestion()

        addView(binding.root)
        setOnTouchListener(this)

        layoutParams.x = x
        layoutParams.y = y

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(this, layoutParams)
        questionRepository.resetTimer()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {
                x = layoutParams.x
                y = layoutParams.y
                onChangeX = event.rawX
                onChangeY = event.rawY
            }

            //Change the position of the widget
            MotionEvent.ACTION_MOVE -> {
                layoutParams.x = (x + event.rawX - onChangeX).toInt()
                layoutParams.y = (y + event.rawY - onChangeY).toInt()
                windowManager.updateViewLayout(this, layoutParams)
            }

        }

        return true
    }

    private fun bindOnQuestion() {
        val question = questionRepository.question.value
        val  url = serverUtils.getServerUrl("$QUESTION_ENDPOINT/${question.id}")

        with(binding) {

            // Establece los elementos relacionados con la pregunta
            questionTypeIcon.setImageResource(question.icon)
            qrCode.setImageBitmap(QRCodeGenerator().encodeAsBitmap(url))
            this.question.text = question.question
            questionUrl.text = url

            // Establece las acciones de los botones
            closeButton.setOnClickListener {
                windowManager.removeView(this@LaunchQuestionView)
                usersRepository.clearRespondedUsers()

                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }

            // Gestión del cronómetro
            if(question.timeOut!! > 0) { setChronometerCount(question.timeOut) }
            timeOutButton.setOnClickListener {
                setDownloadButtonClickable()
                questionRepository.timeOut()
                chronometer.cancelTimer()
            }

            setUsersCount()
            setGraphicAnswersCount(question)
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun ServiceLaunchQuestionBinding.setChronometerCount(timeOut: Int) {
        // Muestra el cronómetro si la pregunta tiene un tiempo de espera
        chronometerTitle.visibility = VISIBLE
        chronometer.visibility = VISIBLE

        val timeInSeconds = timeOut * 60
        val timeInMillis = timeInSeconds * 1000L

        chronometer.setTimeInMillis(timeInMillis)
        chronometer.startCountDown()

        // Lanzamos una corrutina para recoger los cambios en el estado del cronómetro
        GlobalScope.launch {
            chronometer.isFinished.collect { isFinished ->
                if (isFinished) {
                    setDownloadButtonClickable()
                }
            }
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun ServiceLaunchQuestionBinding.setUsersCount() {
        // Lanza una corrutina para recoger los cambios en el número de usuarios que han respondido
        GlobalScope.launch {
            usersRepository.respondedUsers.collect { usersList ->
                withContext(Dispatchers.Main) {
                    respondedUsersCount.text =
                        "${context.getString(R.string.users_count_title)}${usersList.size}"
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun ServiceLaunchQuestionBinding.setGraphicAnswersCount(question: Question) {
        // Lanza una corrutina para recoger los recuentos de respuestas y establecerlos en el grafico de barras.
        GlobalScope.launch {
            question.answers.collect { answers ->
                barView.clearBars()

                answers.forEach { answer ->
                    val bar = Bar(answer.answer.toString(), height = answer.count.value)
                    barView.addBar(bar)

                    GlobalScope.launch {
                        // Recoge los cambios en count para cada respuesta
                        answer.count.collect { count ->
                            bar.height = count
                            barView.invalidate()
                        }
                    }
                }
            }
        }
    }

    private fun ServiceLaunchQuestionBinding.setDownloadButtonClickable() {
        downloadButton.drawable.setTint(
            ContextCompat.getColor(
                context,
                R.color.blue_selected
            )
        )

        downloadButton.setOnClickListener {
            windowManager.removeView(this@LaunchQuestionView)
            usersRepository.clearRespondedUsers()

            val intent = Intent(context, DownloadActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

}