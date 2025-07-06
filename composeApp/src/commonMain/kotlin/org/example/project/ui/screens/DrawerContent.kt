package org.example.project.ui.screens

import androidx.compose.animation.core.copy
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.data.models.AppState
import org.example.project.data.models.DialogScreen
import org.example.project.data.models.PathPoint

@Composable
fun DrawerContent(
    appState: AppState,
    locus: MutableList<PathPoint>,
    onStateChange: (AppState) -> Unit,
    onLocusAdd: (PathPoint) -> Unit,
    onDisplayClear: () -> Unit,
    onDisplayExport: () -> Unit
) {
    ModalDrawerSheet {
        Surface(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            when (appState.currentScreen) {
                is DialogScreen.Main -> {
                    MainScreenContent(
                        appState = appState,
                        locus = locus,
                        onStateChange = onStateChange,
                        onLocusAdd = onLocusAdd,
                        onDisplayClear = onDisplayClear,
                        onDisplayExport = onDisplayExport
                    )
                }
                is DialogScreen.PenSize -> {
                    PenSizeScreenContent(
                        currentRadius = appState.penSize,
                        onPenSizeChange = { newPenSize ->
                            onStateChange(
                                appState.copy(
                                    isPlaying = false,
                                    penSize = newPenSize
                                )
                            )
                        },
                        onNavigateBack = {
                            onStateChange(appState.copy(currentScreen = DialogScreen.Main))
                        }
                    )
                }
                else -> {}
            }
        }
    }
}