package com.orbys.vote.data.controllers

import com.orbys.vote.data.controllers.handlers.ErrorHandler
import com.orbys.vote.data.controllers.handlers.FileHandler
import com.orbys.vote.data.controllers.handlers.ResponseHandler
import io.ktor.server.routing.Route
import javax.inject.Inject

/**
 * Controlador de las rutas del servidor http
 *
 * @param responseHandler Gestor de las respuestas del servicio http
 * @param fileHandler Gestor de los archivos del servicio http
 * @param errorHandler Gestor de los errores del servicio http
 */
class HttpController @Inject constructor(
    private val responseHandler: ResponseHandler,
    private val fileHandler: FileHandler,
    private val errorHandler: ErrorHandler
) {

    fun setupRoutes(route: Route) {

        route.apply {
            // Respuestas del servidor http
            responseHandler.setupRoutes(this)

            // Descargar los datos del servidor
            fileHandler.setupRoutes(this)

            // Gestionar errores
            errorHandler.setupRoutes(this)
        }

    }

    fun clearDataFile() { fileHandler.deleteFile() }
}