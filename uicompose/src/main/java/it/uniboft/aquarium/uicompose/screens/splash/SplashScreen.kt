package it.uniboft.aquarium.uicompose.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics


@Composable
fun SplashScreen(
    state: SplashState,
    onNavigateToHome: () -> Unit,
    onNavigateToScanner: () -> Unit
) {
    // Reagisce ai cambiamenti di stato per triggerare la navigazione
    LaunchedEffect(state) {
        when (state) {
            is SplashState.NavigateToHome -> onNavigateToHome()
            is SplashState.NavigateToScanner -> onNavigateToScanner()
            SplashState.Loading -> { /* Mantiene la UI di caricamento */ }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Avvio dell'applicazione in corso" },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
