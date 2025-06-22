package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.Composable
import org.example.project.AppTheme


// アプリ起動時に一度だけプラットフォーム固有の実装を取得する
private val platform = getPlatform()

@Composable
fun App() {
    AppTheme {
        // TextFieldの入力値を保持するためのState
        var name by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
//            Text("あなたの名前を入力してください")
//            Spacer(Modifier.height(8.dp))
//            TextField(
//                value = name,
//                onValueChange = { name = it },
//                placeholder = { Text("名前") }
//            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                // プラットフォーム固有のオブジェクトのメソッドを呼び出す
                platform.showGreeting(name)
            }) {
                Text("挨拶する (${platform.name})") // ボタンにもプラットフォーム名を表示してみる
            }
        }
    }
}