package com.example.mathia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MathiaMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("FCM TOKEN REFRESHED: $token")
        val sharedPref = getSharedPreferences("mathia_prefs", MODE_PRIVATE)
        val studentPin = sharedPref.getString("student_pin", null)
        if (studentPin != null) {
            sharedPref.edit().putString("fcm_token", token).apply()
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val title = remoteMessage.notification?.title ?: data["title"] ?: "¡Tienes un nuevo desafío!"
        val body = remoteMessage.notification?.body ?: data["body"] ?: "Un compañero te ha desafiado a un duelo matemático."
        val duelId = data["duelId"] ?: ""
        val screen = data["screen"] ?: "duelos"

        mostrarNotificacion(title, body, duelId, screen)
    }

    private fun mostrarNotificacion(
        titulo: String,
        mensaje: String,
        duelId: String = "",
        targetScreen: String = "duelos"
    ) {
        val channelId = "DUEL_NOTIFICATIONS_CHANNEL"
        val channelName = "Duelos MathIA"

        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para desafíos de duelos entre estudiantes MathIA"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("screen", targetScreen)
            putExtra("duelId", duelId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}