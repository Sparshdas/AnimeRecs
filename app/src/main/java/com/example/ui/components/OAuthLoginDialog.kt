package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AnimeCyan
import com.example.ui.theme.AnimeViolet
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OAuthLoginDialog(
    authUrl: String,
    onTokenExtracted: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isWebViewLoading by remember { mutableStateOf(true) }
    var hasIntercepted by remember { mutableStateOf(false) }
    var hasRenderError by remember { mutableStateOf(false) }
    var retryKey by remember { mutableIntStateOf(0) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    BackHandler(onBack = onDismiss)

    DisposableEffect(retryKey) {
        onDispose {
            try {
                webViewInstance?.apply {
                    stopLoading()
                    loadUrl("about:blank")
                    removeAllViews()
                    destroy()
                }
            } catch (_: Exception) {
            }
            webViewInstance = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, AnimeViolet.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxSize(0.90f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .testTag("oauth_login_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AnimeViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = AnimeViolet,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Connect AniList",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "OAuth Implicit Grant Flow",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_oauth_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // In-App WebView with automatic redirect token interception
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    when {
                        hasIntercepted -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(DarkSurfaceCard)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AnimeCyan,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Authorization Successful!",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Connecting your AniList account...",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        hasRenderError -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(DarkSurfaceCard)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AnimeViolet,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Login View Interrupted",
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "The embedded browser process was reset by the system. Tap below to reload the AniList login page.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = {
                                        hasRenderError = false
                                        isWebViewLoading = true
                                        retryKey++
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AnimeViolet),
                                    modifier = Modifier.testTag("reload_oauth_webview_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reload Login Page", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        else -> {
                            key(retryKey) {
                                AndroidView(
                                    factory = { ctx ->
                                        WebView(ctx).apply {
                                            webViewInstance = this
                                            // Disable hardware acceleration on webview to prevent GPU driver/render-process drops in container
                                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true
                                            settings.databaseEnabled = false
                                            settings.setSupportZoom(false)
                                            settings.builtInZoomControls = false
                                            settings.displayZoomControls = false
                                            settings.mediaPlaybackRequiresUserGesture = true
                                            settings.userAgentString =
                                                "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

                                            webViewClient = object : WebViewClient() {
                                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                    super.onPageStarted(view, url, favicon)
                                                    if (url != null && checkAndInterceptUrl(url)) {
                                                        view?.stopLoading()
                                                        return
                                                    }
                                                    isWebViewLoading = true
                                                }

                                                override fun onPageFinished(view: WebView?, url: String?) {
                                                    super.onPageFinished(view, url)
                                                    isWebViewLoading = false
                                                    url?.let { checkAndInterceptUrl(it) }
                                                }

                                                override fun doUpdateVisitedHistory(
                                                    view: WebView?,
                                                    url: String?,
                                                    isReload: Boolean
                                                ) {
                                                    super.doUpdateVisitedHistory(view, url, isReload)
                                                    if (url != null && checkAndInterceptUrl(url)) {
                                                        view?.stopLoading()
                                                    }
                                                }

                                                override fun shouldOverrideUrlLoading(
                                                    view: WebView?,
                                                    request: WebResourceRequest?
                                                ): Boolean {
                                                    val url = request?.url?.toString() ?: return false
                                                    if (checkAndInterceptUrl(url)) {
                                                        view?.stopLoading()
                                                        return true
                                                    }
                                                    return false
                                                }

                                                override fun onReceivedError(
                                                    view: WebView?,
                                                    request: WebResourceRequest?,
                                                    error: WebResourceError?
                                                ) {
                                                    val failingUrl = request?.url?.toString()
                                                    if (failingUrl != null && checkAndInterceptUrl(failingUrl)) {
                                                        view?.stopLoading()
                                                        return
                                                    }
                                                    val currentUrl = view?.url
                                                    if (currentUrl != null && checkAndInterceptUrl(currentUrl)) {
                                                        view.stopLoading()
                                                        return
                                                    }
                                                    super.onReceivedError(view, request, error)
                                                    if (request?.isForMainFrame == true) {
                                                        isWebViewLoading = false
                                                    }
                                                }

                                                override fun onRenderProcessGone(
                                                    view: WebView?,
                                                    detail: RenderProcessGoneDetail?
                                                ): Boolean {
                                                    // Prevent app crash if system WebView renderer drops or crashes in emulator
                                                    try {
                                                        view?.stopLoading()
                                                        view?.removeAllViews()
                                                        view?.destroy()
                                                    } catch (_: Exception) {
                                                    }
                                                    if (webViewInstance === view) {
                                                        webViewInstance = null
                                                    }
                                                    isWebViewLoading = false
                                                    hasRenderError = true
                                                    return true
                                                }

                                                private fun checkAndInterceptUrl(url: String): Boolean {
                                                    if (url.contains("access_token=") || url.startsWith("http://localhost:3000")) {
                                                        if (!hasIntercepted && url.contains("access_token=")) {
                                                            hasIntercepted = true
                                                            onTokenExtracted(url)
                                                        }
                                                        return true
                                                    }
                                                    return false
                                                }
                                            }

                                            loadUrl(authUrl)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            if (isWebViewLoading) {
                                LinearProgressIndicator(
                                    color = AnimeViolet,
                                    trackColor = DarkSurfaceElevated,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.TopCenter)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Auto-extracts token on redirect to localhost:3000",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

