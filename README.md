# Java CDC Agent

MySQLデータベースの変更データキャプチャ（CDC）を行い、HULFT SquareにREST APIを通じてデータを送信するエージェント。

## 概要

このプロジェクトは、Debeziumを利用してMySQLデータベースの変更データキャプチャ（CDC）を行い、取得したデータをiPaaSであるHULFT SquareにREST APIを通じて送信するエージェントを提供します。Java 21で開発され、Spring Framework、Project Reactor、Debeziumなどの最新技術を活用しています。

## 機能

- **データベース対応**: MySQLデータベースの変更データキャプチャに対応
- **リアルタイムデータキャプチャ**: Debezium MySQLコネクタを使用したデータベースの監視
- **データ送信**: HULFT SquareのREST APIエンドポイントへのデータ送信
- **バックプレッシャー制御**: HULFT Squareの処理能力に応じたデータ送信速度の調整
- **エージェントの制御**: HULFT Squareからの指示による一時停止および終了機能
- **監視とロギング**: 稼働状況の監視、ログの収集・分析機能

## 技術スタック

- **プログラミング言語**: Java 21
- **フレームワーク**: Spring Boot 3.x
- **CDCプラットフォーム**: Debezium
- **リアクティブプログラミング**: Project Reactor
- **HTTPクライアント**: Spring WebFlux
- **ロギング**: SLF4J、Logback
- **監視**: Micrometer、Prometheus、Grafana
- **ビルドツール**: Gradle

## ビルドと実行

### 前提条件

- Java 21
- Gradle 8.6+ (Gradle Wrapperを使用する場合は不要)
- MySQL 8.0+ (バイナリログが有効になっていること)

### ビルド方法

```bash
./gradlew clean build
```

### 実行方法

```bash
java -jar build/libs/java-cdc-agent-1.0.0.jar
```

または

```bash
./gradlew bootRun
```

## 設定

`application.yml`ファイルで以下の設定を行えます：

### Debezium設定

```yaml
debezium:
  connector:
    name: mysql-connector
    offset.storage: org.apache.kafka.connect.storage.FileOffsetBackingStore
    offset.storage.file.filename: ${user.home}/offsets.dat
    offset.flush.interval.ms: 60000
  source:
    database:
      hostname: localhost
      port: 3306
      user: debezium
      password: dbz
      server-id: 1
      server-name: mysql-server-1
      include-schema-changes: true
```

### HULFT Square設定

```yaml
hulft:
  square:
    api:
      url: http://localhost:9000/api/events
      connectTimeout: 5000
      readTimeout: 5000
      writeTimeout: 5000
      maxInFlight: 100
      retryCount: 3
      retryBackoffMs: 1000
```

## APIエンドポイント

エージェントの制御のために以下のエンドポイントが提供されています：

- `GET /api/agent/status` - エージェントの現在の状態を取得
- `POST /api/agent/start` - エージェントを起動
- `POST /api/agent/pause` - エージェントを一時停止
- `POST /api/agent/resume` - 一時停止したエージェントを再開
- `POST /api/agent/stop` - エージェントを停止

## モニタリング

Prometheus対応のメトリクスが `/actuator/prometheus` エンドポイントで提供されています。

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。詳細はLICENSEファイルを参照してください。 