package org.example.project.data.models

/**
 * ドラッグ操作のモードを表すEnum
 * スピログラフの各部品をドラッグする際の操作種別を定義する
 */
enum class DraggingMode{
    /** ドラッグ操作なし */
    NONE,
    
    /** スパー（外側の歯車）の中心を移動 */
    MOVE_SPUR_CENTER,
    
    /** スパーの半径をリサイズ */
    RESIZE_SPUR_RADIUS,
    
    /** ピニオン（内側の歯車）の半径をリサイズし、中心を移動 */
    RESIZE_PINION_RADIUS_AND_MOVE_CENTER,
    
    /** ペンの位置を移動 */
    MOVE_PEN,
    
    /** 全体をパン（平行移動） */
    PAN
}