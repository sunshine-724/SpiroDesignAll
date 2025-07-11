package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker

/**
 * 色選択コントロールのComposable関数
 * 現在の色を表示し、HSVカラーピッカーで新しい色を選択できるUI要素を提供する
 *
 * @param currentColor 現在選択されている色
 * @param controller カラーピッカーのコントローラー
 * @param onColorChange 色が変更された時のコールバック
 */
@Composable
public fun ColorControl(
    currentColor: Color,
    controller: ColorPickerController,
    onColorChange: (Color) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Current Color", fontSize = 18.sp)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(currentColor)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Choose Color", fontSize = 18.sp)
            HsvColorPicker(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                controller = controller,
                onColorChanged = { colorEnvelope ->
                    onColorChange(colorEnvelope.color)
                },
                initialColor = currentColor,
            )
        }
    }
}