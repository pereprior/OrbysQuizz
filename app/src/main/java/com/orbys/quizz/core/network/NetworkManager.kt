package com.orbys.quizz.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.orbys.quizz.R
import com.orbys.quizz.core.extensions.showToastWithCustomView
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

/**
 * Clase para gestionar todas las operaciones relacionadas con la red.
 */
class NetworkManager {

    // Devuelve la dirección IP local del dispositivo que ejecuta la app
    fun getLocalIpAddress() = try { getLocalIpFromNetworkInterfaces() } catch (ex: Exception) { null }

    fun checkNetwork(activity: AppCompatActivity) {
        if (!isNetworkAvailable(activity)) {
            activity.showToastWithCustomView(activity.getString(R.string.no_network_error), Toast.LENGTH_LONG)
            activity.finish()
            return
        }
    }

    private fun isNetworkAvailable(activity: AppCompatActivity): Boolean {
        val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Comprueba si hay una conexión a Internet activa
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        // Comprueba si la conexión a Internet activa tiene la capacidad de conectarse a Internet
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getLocalIpFromNetworkInterfaces(): String? {
        // Obtengo una lista de todas las interfaces de red en el dispositivo
        val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())

        for (i in interfaces) {
            // Obtengo una lista de todas las direcciones IP asociadas a la interfaz
            val addresses = Collections.list(i.inetAddresses)


            for (address in addresses) {
                // Si la direccion IP no es loopback y es IPv4, la retornamos
                if (address.isNonLoopbackIPv4Address()) {
                    return address.hostAddress
                }
            }
        }

        return null
    }

    // Comprueba si la direccion es IPv4 no loopback
    private fun InetAddress.isNonLoopbackIPv4Address() = !isLoopbackAddress && this is Inet4Address
}