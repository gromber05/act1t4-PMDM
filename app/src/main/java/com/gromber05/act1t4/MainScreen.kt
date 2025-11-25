package com.gromber05.act1t4

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.SoundPool
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.IOException

private const val AUDIO_URL = "https://www.example.com/audio_largo.mp3"
private const val VIDEO_URL = "https://www.example.com/video_demo.mp4"

@SuppressLint("Range")
@Composable
fun MainScreen() {
    val context = LocalContext.current

    var soundLoaded by remember { mutableStateOf(false) }
    var soundId by remember { mutableStateOf(0) }

    val soundPool = remember {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(1)
            .build()
    }

    LaunchedEffect(Unit) {
        soundId = soundPool.load(context, R.raw.click_sound, 1)
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (sampleId == soundId && status == 0) {
                soundLoaded = true
            }
        }
    }

    val audioPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(AUDIO_URL)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    val videoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(VIDEO_URL)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    val takePictureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                saveImage(context, bitmap)
            } else {
                Toast.makeText(context, "No se ha tomado ninguna foto", Toast.LENGTH_SHORT).show()
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                takePictureLauncher.launch(null)
            } else {
                Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }

    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
            audioPlayer.release()
            videoPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Button(
            onClick = {
                if (soundLoaded) {
                    soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                } else {
                    Toast.makeText(context, "Sonido aún no cargado", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .weight(0f, false)
        ) {
            Text(text = "Reproducir sonido corto (SoundPool)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Audio remoto (ExoPlayer)", style = MaterialTheme.typography.titleMedium)

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = audioPlayer
                    useController = true
                }
            },
            modifier = Modifier
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        RowButtons(
            onPlay = { audioPlayer.play() },
            onPause = { audioPlayer.pause() },
            playText = "Play audio remoto",
            pauseText = "Pause audio"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Vídeo remoto (ExoPlayer)", style = MaterialTheme.typography.titleMedium)

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = videoPlayer
                    useController = true
                }
            },
            modifier = Modifier
                .padding(top = 8.dp)
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        RowButtons(
            onPlay = { videoPlayer.play() },
            onPause = { videoPlayer.pause() },
            playText = "Play vídeo remoto",
            pauseText = "Pause vídeo"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (granted) {
                    takePictureLauncher.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .weight(0f, false)
        ) {
            Text(text = "Abrir cámara y guardar foto")
        }
    }
}

private fun saveImage(context: Context, bitmap: Bitmap) {
    val fileName = "foto_${System.currentTimeMillis()}.jpg"
    try {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        }
        Toast.makeText(context, "Foto guardada como $fileName", Toast.LENGTH_LONG).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Error al guardar la foto", Toast.LENGTH_SHORT).show()
    }
}
