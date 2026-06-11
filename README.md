# android-app-hub

カスタム URL スキームを持たない Android アプリを、URL から起動するための中継アプリ。

Todoist などのタスク管理アプリに `apphub://` 形式のリンクを書いておくと、タップで任意のアプリへ遷移できる。

## 使い方

```
apphub://<パッケージ名>
```

例: Todoist のタスクやコメントに以下のリンクを書く。

```
[Google Keep を開く](apphub://com.google.android.keep)
```

タップすると本アプリが URI を受け取り、パッケージ名のアプリを起動して即終了する。UI は持たない（ゼロコンフィグ）。

- 対象アプリが見つからない場合は Toast でパッケージ名を通知する
- パッケージ名は Play ストアのアプリページ URL の `id=` パラメーターで確認できる
  （例: `https://play.google.com/store/apps/details?id=com.todoist` → `com.todoist`）

## ビルドとインストール

```sh
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

要件: JDK 17+、Android SDK (compileSdk 35)。

## 設計メモ

- 依存ライブラリゼロ（androidx 不使用）。`Theme.NoDisplay` の単一 Activity のみ
- Android 11+ のパッケージ可視性は `<queries>`（MAIN + LAUNCHER）で対応し、
  `QUERY_ALL_PACKAGES` 権限は使わない
- URI のパース処理は `DeepLink.extractPackageName` に分離し、JUnit でテスト
