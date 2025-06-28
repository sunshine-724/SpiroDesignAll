/**
 * JavaにあるStringFormatのJavaScriptバージョン(ただし、今の所数値のみ)
 * @param formatString
 * @param args
 * @returns {*}
 */

function stringFormatForJavaScript(formatString, ...args) {
    let i = 0;
    return formatString.replace(/%(?:0(\d+))?([Xx])/g, (match, width, type) => {
        const arg = args[i++];
        if (typeof arg === 'number') { // 引数が数値であることを確認
            let hex = arg.toString(16); // 16進数に変換
            if (type === 'X') hex = hex.toUpperCase(); // 大文字のXなら大文字に
            if (type === 'x') hex = hex.toLowerCase(); // 小文字のxなら小文字に

            if (width) {
                // 幅指定 (例: %08X の '08') に合わせてゼロ埋め
                hex = hex.padStart(parseInt(width), '0');
            }
            return hex;
        }
        // 数値以外、または未対応の書式指定子の場合
        return match;
    });
}