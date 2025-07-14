package org.example.project

// --- ▼▼▼ ワイルドカードで整理したリスト ▼▼▼ ---
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import com.github.skydoves.colorpicker.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.SelectClause0
import org.example.project.data.models.AppState
import org.example.project.data.models.DeviceType.*
import org.example.project.data.models.PathPoint
import org.example.project.data.utils.handleExportAction
import org.example.project.ui.components.DrawingCanvas
import org.example.project.ui.screens.DrawerContent
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.*

// --- ▲▲▲ ここまで ▲▲▲ ---

// アプリ起動時に一度だけプラットフォーム固有の実装を取得する
val platform = getPlatform()


/**
 * App
 *
 */
@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun App() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // 設定画面を開いているか閉じているかどうか(PC用)

    val scope = rememberCoroutineScope()

    // アプリケーション状態を管理
    var appState by remember {
        mutableStateOf(AppState())
    }
    val locus = remember { mutableStateListOf<PathPoint>() }

    /**
     * Cumulative scale 拡大率を保持します
     */
    var cumulativeScale by remember { mutableStateOf(1.0f) }
    val onScaleChange: (Float) -> Unit = { scale ->
        cumulativeScale = scale
    }

    LaunchedEffect(appState.isPlaying) {
        // isPlayingがfalseになり、かつ軌跡が1点以上ある場合
        if (!appState.isPlaying && locus.isNotEmpty()) {
            // 最後の点をコピーして、色が透明なだけの新しい点を作る
            val blankPoint = locus.last().copy(color = Color.Transparent)
            // 軌跡の末尾に追加
            locus.add(blankPoint)
        }
    }

    AppTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            when(platform.getDeviceType()) {
                DESKTOP ->
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            DrawerContent(
                                appState = appState,
                                locus = locus,
                                onStateChange = { newState -> appState = newState },
                                onLocusAdd = { newPoint -> locus.add(newPoint) },
                                onDisplayClear = {
                                    appState = appState.copy(isPlaying = false)
                                    locus.clear()
                                },
                                onDisplayExport = {
                                    handleExportAction(
                                        scope = scope,
                                        drawerState = drawerState,
                                        onStateChange = { newState -> appState = newState },
                                        platform = platform
                                    )
                                }
                            )
                        }
                    ) {
                        MainContent(
                            appState = appState,
                            locus = locus,
                            scope = scope,
                            drawerState = drawerState,
                            onLocusAdd = { newPoint -> locus.add(newPoint) },
                            cumulativeScale = cumulativeScale,
                            onScaleChange = onScaleChange,
                            platform = platform,
                        )
                    }
                IOS -> {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            DrawerContent(
                                appState = appState,
                                locus = locus,
                                onStateChange = { newState -> appState = newState },
                                onLocusAdd = { newPoint -> locus.add(newPoint) },
                                onDisplayClear = {
                                    appState = appState.copy(isPlaying = false)
                                    locus.clear()
                                },
                                onDisplayExport = {
                                    handleExportAction(
                                        scope = scope,
                                        drawerState = drawerState,
                                        onStateChange = { newState -> appState = newState },
                                        platform = platform
                                    )
                                }
                            )
                        }
                    ) {
                        MainContent(
                            appState = appState,
                            locus = locus,
                            scope = scope,
                            drawerState = drawerState,
                            onLocusAdd = { newPoint -> locus.add(newPoint) },
                            cumulativeScale = cumulativeScale,
                            onScaleChange = onScaleChange,
                            platform = platform,
                        )
                    }
                }
                ANDROID -> {
                }
                WEB -> ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent(
                            appState = appState,
                            locus = locus,
                            onStateChange = { newState -> appState = newState },
                            onLocusAdd = { newPoint -> locus.add(newPoint) },
                            onDisplayClear = {
                                appState = appState.copy(isPlaying = false)
                                locus.clear()
                            },
                            onDisplayExport = {
                                handleExportAction(
                                    scope = scope,
                                    drawerState = drawerState,
                                    onStateChange = { newState -> appState = newState },
                                    platform = platform
                                )
                            }
                        )
                    }
                ) {
                    MainContent(
                        appState = appState,
                        locus = locus,
                        scope = scope,
                        drawerState = drawerState,
                        onLocusAdd = { newPoint -> locus.add(newPoint) },
                        cumulativeScale = cumulativeScale,
                        onScaleChange = onScaleChange,
                        platform = platform,
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainContent(
    appState: AppState,
    locus: MutableList<PathPoint>,
    scope: CoroutineScope,
    drawerState: DrawerState,
    onLocusAdd: (PathPoint) -> Unit,
    cumulativeScale: Float,
    onScaleChange: (Float) -> Unit,
    platform: Platform,
) {

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DrawingCanvas(
            color = appState.currentColor,
            speed = appState.spurSpeed,
            isPlaying = appState.isPlaying,
            isExporting = appState.isExporting,
            locus = locus,
            penSize = Stroke(appState.penRadius),
            onAddPoint = onLocusAdd,
            cumulativeScale = cumulativeScale,
            onScaleChange = { newScale ->
                onScaleChange(newScale)
            },
            platform = platform,
            onOpenMenu = { scope.launch { drawerState.open() } }
        )
    }
}

/**
 * Drag info
 *
 * @param position
 * @param color
 * @param radius
 */
@Composable
fun DragInfo(position: Offset, color: Color, radius: Float) {
    TODO("Not yet implemented")
}
