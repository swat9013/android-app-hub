# ADR 0002: 公開用リポジトリを分けず、本リポジトリを public 化して配布する

## ステータス

Accepted

## コンテキスト

ADR 0001 で配布先を GitHub Releases に決め、tag push で署名済み APK が自動公開される
状態になった。しかしリポジトリが private のままだったため、release asset の取得には
リポジトリへのアクセス権が必要で、エンドユーザー（Todoist 等から本アプリを呼び出す
一般利用者）は誰も APK を入手できなかった。v1.0.0 の APK ダウンロード数は 2 で、
実質的に開発者本人にしか届いていない。

Google Play ストアを使わない方針は ADR 0001 で決定済み（新規個人デベロッパー
アカウントには 12 人のテスターによる 14 日間のクローズドテストが要求され、
ニッチな中継アプリの運用コストに見合わない）。したがって論点は「Play 以外でどう
届けるか」ではなく「既にある GitHub Releases をどう到達可能にするか」だった。

当初は「開発用の private リポジトリと公開用のリポジトリを分ける」案を検討した。
分離の動機は「試行錯誤の commit 履歴を公開したくない」だったが、実際の履歴を
確認したところ commit は 14 本ですべて conventional commit、issue は 4 件で
すべて CI / リリース整備の実務であり、隠す対象が存在しなかった。

## 決定

- 公開用リポジトリを別に作らず、`swat9013/android-app-hub` 自体を public にする
- MIT License（`Copyright (c) 2026 swat9013`）を `LICENSE` として追加する。
  ライセンス表記のないリポジトリは全権利留保であり、公開しても利用を許諾したことに
  ならないため、public 化の前提条件として扱う
- 既存の v1.0.0 Release はそのまま維持する。public 化した時点で同じ URL の APK が
  匿名ダウンロード可能になり、再リリースは不要
- GitHub Issues は有効のまま残す（`docs/agents/issue-tracker.md` の agent 運用が
  ここに乗っているため）
- リポジトリの description と topics（`android` / `kotlin` / `deep-link` /
  `url-scheme`）を設定する

## 棄却案

1. **公開用リポジトリを別に作り、履歴なしの snapshot を置く**
   - 却下理由: 分離の動機だった「隠したい履歴」が実在しなかった。動機が消えると、
     残るのは二重管理のコスト（リリース作業・secrets・README の同期）だけになる
2. **private のまま、必要な人にだけコラボレーターとして招待する**
   - 却下理由: 招待という手間が受け取り側にも自分にも残り、「URL を踏めば使える」
     という本アプリの設計方針（ゼロコンフィグ）と噛み合わない
3. **Obtainium（GitHub Releases を監視する OSS の自動更新クライアント）の導線を
   README に追加する**
   - 却下理由: 自動更新は魅力的だが、利用者に Obtainium 自体のインストールを
     要求する。public 化の効果を見てから判断すればよく、今入れる必要がない
4. **F-Droid / IzzyOnDroid への収録**
   - 却下理由: 依存ライブラリゼロ・GMS 不使用という点で収録要件は満たすが、
     審査待ちの時間を先に払うことになる。public 化だけで配布は成立するため、
     発見性を上げたくなった段階で改めて検討する

## 影響

- リポジトリの全履歴・全 issue・全 PR が公開される。public 化は事実上不可逆
  （fork やアーカイブにより第三者の手元に残り得る）
- 第三者から issue / PR を受け取り得る。fork からの pull request では
  `ci.yml` が動くが、この workflow は secrets を使わない。署名鍵を使う
  `release.yml` は tag push でのみ起動するため、fork 経由では実行されない
- Actions は public リポジトリでは追加課金なく実行できる
- 将来 F-Droid / IzzyOnDroid を狙う場合、ソース公開と FLOSS ライセンスという
  前提条件は本 ADR で満たされる
