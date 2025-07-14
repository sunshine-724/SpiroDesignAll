## アプリケーション実行ガイド(クライアント向け)
このガイドでは、SpiroDesignアプリケーションをさまざまなプラットフォームで利用する方法を説明します。
### 🌐 Web版
   以下のURLからすぐにアプリケーションにアクセスして利用できます。  
   https://sunshine-724.github.io/SpiroDesignAll/

#### デモ動画
   https://github.com/user-attachments/assets/bdc7cf71-0901-4b31-a661-b14960fee12e
   
### 📱 スマホ版・タブレット版  
   IOS版では自分のiPhone15(IOS 26.0)で確認済みです
---
## アプリケーション実行ガイド(開発者向け)
このガイドは、各プラットフォームでアプリケーションをビルドし、実行するための手順をまとめたものです。
### 0. 前提条件
   開発を始める前に、お使いのPCに以下のツールがインストールされていることを確認してください。  
   JDK (Java Development Kit) 17以上  
   Android Studio: Androidアプリのビルドと、Android SDKの管理に必要です。  
   IntelliJ IDEA または JetBrains Fleet: 開発用IDEです。  
   Xcode: iOSアプリのビルドとシミュレータの実行に必要です（macOSのみ）。  
### 1. Android
   GUI (IDE)での実行
   IntelliJ IDEA / Fleetの画面右上にある実行構成ドロップダウンから composeApp を選択します。  
   隣のデバイス選択メニューで、起動したいAndroidエミュレータか、PCに接続している実機を選択します。  
   緑色の**再生ボタン（▶️ Run）**をクリックします。  
   CUI (ターミナル)での実行
   プロジェクトのルートディレクトリでターミナルを開きます。  
   以下のコマンドを実行して、アプリをビルドし、接続されているデバイスにインストールします。

   必要であればlocal.propertiesにAndroidSDKのパスを通してください  
   ↓記述例(Mac)  
   sdk.dir=/User/{USERNAME}/Library/Android/sdk
```
./gradlew :composeApp:installDebug
```
インストール後、デバイスのアプリ一覧から手動でアプリを起動してください。
### 2. iOS (macOS必須)
   GUI (IDE)での実行  
   IntelliJ IDEA / Fleetの画面右上にある実行構成ドロップダウンから iosApp を選択します。  
   隣のデバイス選択メニューで、好きなiOSシミュレータ（例: iPhone 15 Pro）を選択します。  
   緑色の**再生ボタン（▶️ Run）**をクリックします。（初回のビルドは非常に時間がかかります）  
   ヒント:  
   一度この方法で実行すると、composeApp/iosApp/ ディレクトリに iosApp.xcworkspace というファイルが生成されます。  
   以降は、このファイルを直接Xcodeで開いてビルド・実行することも可能です。  
   CUI (ターミナル)での実行  
   コマンドラインから直接シミュレータを起動するのは複雑なため、ビルドとXcodeでの起動を組み合わせるのが現実的です。  
   プロジェクトのルートでターミナルを開き、以下のコマンドでXcodeプロジェクトをビルド・生成します。  
```
./gradlew :composeApp:build
```

ビルドが成功したら、Finderで composeApp/iosApp/iosApp.xcworkspace を見つけ、ダブルクリックしてXcodeで開きます。
Xcodeの画面左上でシミュレータを選択し、再生ボタン（▶️）を押してアプリを実行します。

アプリ化してリリースしたい場合は以下のコマンドからできます
```
./gradlew :composeApp:linkReleaseFrameworkIosArm64
```
### 3. Desktop (JVM)
   GUI (IDE)での実行
   IntelliJ IDEA / Fleetの画面右上にある実行構成ドロップダウンから desktopApp を選択します。
   緑色の**再生ボタン（▶️ Run）**をクリックします。
   CUI (ターミナル)での実行
   以下のコマンドで、デスクトップアプリケーションを直接起動できます。
```
./gradlew :composeApp:run
```

### 4. Web (Wasm)
   GUI (IDE)での実行
   IntelliJ IDEAの画面右側にあるGradleタブ（象のアイコン）を開きます。
   ツリーを :composeApp -> Tasks -> kotlin browser と辿ります。
   wasmJsBrowserDevelopmentRun タスクをダブルクリックします。
   ビルドが完了すると、自動的にブラウザで http://localhost:8080 が開かれます。
   CUI (ターミナル)での実行
   以下のコマンドで、開発用のWebサーバーを起動します。
```
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```
ビルドが完了したら、ブラウザで http://localhost:8080 を開いてください。Ctrl + Cでサーバーを停止できます。
### 5. 主要なライブラリバージョン情報 (Key Library Versions)
   このプロジェクトで使用されている主要なライブラリとSDKのバージョンです。
   バージョン確認ファイル
   多くのバージョンは、プロジェクトのルートにある gradle/libs.versions.toml ファイルで一元管理されています。
   #### バージョン一覧
   Kotlin: 2.1.21   
   Compose Multiplatform: 1.8.1  
   Android SDK:  
   compileSdk: 35  
   targetSdk: 35  
   minSdk: 24  
### 6. 実行環境のバージョン (Execution Environment Versions)
   このアプリケーションが動作する各プラットフォームの実行環境です。  
   Desktop (JVM):  
   実行環境: Java Virtual Machine (JDK)  
   バージョン確認: ターミナルで java -version を実行して確認します。  
   Android:  
   実行環境: Android OS  
   バージョン確認: アプリを実行するエミュレータまたは実機の「設定」->「端末情報」で確認します。  
   iOS:  
   実行環境: iOS  
   バージョン確認: アプリを実行するシミュレータまたは実機の「設定」->「一般」->「情報」で確認します。  
   Web (Wasm):  
   実行環境: Webブラウザ  
   バージョン確認: アプリを表示しているブラウザ（Chrome, Safari等）の「設定」メニューから確認します。  
### 7. 開発・検証環境 (Development & Verification Environment)  

このプロジェクトの開発および動作検証は、以下の環境で行いました。  

* **OS:** `macOS Sequoia 15.0`
* **IDE:** `IntelliJ IDEA 2025.1.2`
* **JDK Version:** `21.0.7`
* **Xcode Version:** `Version 16.2`
* **iOS Version:** `iOS 18.3`
* **Android (検証端末):** `medium phone api 36`
* **Web (検証ブラウザ):** `Google Chrome 137.0.7151.120 (arm64)`
