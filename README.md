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

## 開発ビルド

```sh
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

要件: JDK 17+、Android SDK (compileSdk 35)。

## 配布（GitHub Releases）

エンドユーザー向けには、署名付き APK を GitHub Releases で配布する。Google Play には出さない。

### 初回のみ: リリース署名鍵を作成

```sh
keytool -genkeypair -v -keystore upload-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

- `validity 10000` 日 ≒ 27 年。**バージョン間で同じ鍵を使い続ける**こと。鍵かパスワードを失うと、既存インストールへの上書き更新ができなくなる（別アプリ扱いになる）。
- `keystore.properties.example` をコピーして値を埋める:

  ```sh
  cp keystore.properties.example keystore.properties
  # storeFile に .jks の絶対パス、storePassword / keyAlias / keyPassword を記入
  ```

- `keystore.properties` と `*.jks` は `.gitignore` 済みでコミットされない。鍵とパスワードはローカルで安全に保管する。

### リリースごと: APK をビルドしてアップロード

1. `app/build.gradle.kts` の `versionCode`（整数を +1）と `versionName` を上げる
2. 署名付き APK をビルド:

   ```sh
   ./gradlew assembleRelease
   # 出力: app/build/outputs/apk/release/app-release.apk
   ```

   `keystore.properties` があれば自動で署名される（無ければ未署名ビルドになりインストール不可）。
3. GitHub Release を作成し APK を添付:

   ```sh
   VERSION=v1.0.0
   git tag "$VERSION" && git push origin "$VERSION"
   gh release create "$VERSION" \
     app/build/outputs/apk/release/app-release.apk \
     --title "$VERSION" --notes "変更点をここに書く"
   ```

### エンドユーザー: インストール

1. [Releases](../../releases) から最新の `app-release.apk` をダウンロード
2. Android の設定で、使用するブラウザ／ファイルアプリに対して「不明なアプリのインストール」を許可
3. ダウンロードした APK を開いてインストール

## 設計メモ

- 依存ライブラリゼロ（androidx 不使用）。`Theme.NoDisplay` の単一 Activity のみ
- Android 11+ のパッケージ可視性は `<queries>`（MAIN + LAUNCHER）で対応し、
  `QUERY_ALL_PACKAGES` 権限は使わない
- URI のパース処理は `DeepLink.extractPackageName` に分離し、JUnit でテスト
