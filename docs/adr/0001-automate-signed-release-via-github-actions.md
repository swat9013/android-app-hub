# ADR 0001: GitHub Actions と Secrets でリリース署名を自動化する

## ステータス

Accepted

## コンテキスト

これまでのリリース手順は、ローカルで `assembleRelease` を実行して署名済み APK を作り、
`gh release create` で手動アップロードする方式だった（README「配布（GitHub Releases）」節）。
tag を打つたびに毎回同じ手順を人手で踏む必要があり、手順の踏み忘れ（例: `versionName` の
指定漏れ）や、ローカル環境依存で再現性が低いことがリスクだった。

## 決定

- tag push（`v*`）をトリガーに GitHub Actions で `test → assembleRelease（署名付き）→
  署名検証 → gh release create` まで一気通貫で自動化する（`.github/workflows/release.yml`）
- 署名鍵（`ANDROID_KEYSTORE_BASE64`）と鍵パスワード類（`ANDROID_KEYSTORE_PASSWORD` /
  `ANDROID_KEY_ALIAS` / `ANDROID_KEY_PASSWORD`）は GitHub Actions の repository secrets に
  登録する
- workflow は secrets から keystore を `$RUNNER_TEMP` に復元し、`keystore.properties` を
  ワークスペースに生成する。`app/build.gradle.kts` は「`keystore.properties` があれば署名する」
  という既存の分岐にそのまま乗るため、署名まわりのビルドスクリプト変更は不要
- 配布先は GitHub Releases のみとし、Google Play ストアには公開しない

## 棄却案

1. **CI は build/test のみ行い、署名は手元で行う**
   - 却下理由: 結局リリースのたびに手作業のステップが残り、この対応が解決したい
     「tag を打つだけで完結させる」目的を達成できない
2. **unsigned APK をひとまず Release に上げ、必要な人が手元で署名して使う**
   - 却下理由: エンドユーザー（Todoist 等から本アプリを呼び出す一般利用者）に署名という
     技術的なハードルを課すことになり、本アプリの「ゼロコンフィグで使える中継アプリ」
     という設計方針に反する。そもそも未署名 APK はインストールできない
3. **Google Play ストアでの配布**
   - 却下理由: 本アプリはカスタム URL スキームで他アプリを起動する中継アプリという
     ニッチな用途で、審査対応・ストア掲載の運用コストに見合わない。GitHub Releases から
     の直接インストールで想定利用者には十分

## 影響

- リリースは `git tag vX.Y.Z && git push origin vX.Y.Z` のみで完結する
- 署名鍵の実体はリポジトリに一切残らず、GitHub Secrets 経由でのみ実行時に復元される
- `apksigner verify` を workflow に組み込み、未署名 APK が誤って公開されるのを防ぐ
