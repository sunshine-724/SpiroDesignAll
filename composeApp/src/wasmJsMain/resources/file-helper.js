/**
 * 文字列コンテンツからCSV形式のBlobオブジェクトを作成して返す関数
 * @param {string} content - ファイルの中身となる文字列
 * @returns {Blob} 作成されたBlobオブジェクト
 */
function createCsvBlob(content) {
    // JavaScriptのBlobコンストラクタは、第一引数に配列を期待します。
    // 部品が一つでも配列に入れるのがお作法です。
    return new Blob([content], { type: "text/csv" });
}