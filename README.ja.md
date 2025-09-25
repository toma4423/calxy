# Calxy

Calxyは、Microsoft ExcelやAccessの代替となることを目指して設計された、軽量かつ高速な現代的表計算ソフトウェアです。VBAの代わりにPythonによるスクリプト記述をサポートし、エンドユーザーが外部依存（PythonやJVMのインストールなど）を一切気にすることなく利用できる、クロスプラットフォーム（Windows, Mac, Linux）アプリケーションです。

このリポジトリには、Calxyアプリケーションのソースコードが含まれています。

## ✨ 主な特徴

- **軽量・高速:** 効率性を追求した設計。
- **モダンなスクリプト:** マクロや高度なセル関数にPythonを使用可能。
- **互換性:** Excel (`.xlsx`) およびCSVファイルの読み書きに対応。
- **クロスプラットフォーム:** Windows, macOS, Linuxで動作。
- **ゼロ依存インストール:** エンドユーザーはアプリケーション本体をインストールするだけ。

## 🛠️ 開発環境

このプロジェクトは、開発者間の環境差異をなくすため、Dockerベースの開発環境を使用します。

### 前提条件

- [Docker](https://www.docker.com/products/docker-desktop/) がインストールされ、実行中であること。

### セットアップ手順

1.  **リポジトリをクローン:**
    ```bash
    git clone https://github.com/toma4423/calxy.git
    cd calxy
    ```

2.  **開発コンテナのビルドと起動:**
    初期セットアップは完了済みです。開発コンテナはバックグラウンドで実行されています。Docker経яで接続してください。

3.  **コンテナへのアクセス:**
    実行中のコンテナ内でシェルを開くには、以下のコマンドを実行します:
    ```bash
    docker exec -it calxy-app-dev-1 bash
    ```
    プロジェクトファイルは、コンテナ内の `/app` ディレクトリにあります。

4.  **コンテナの停止:**
    コンテナを停止するには、ホストマシンの `calxy` ディレクトリで以下のコマンドを実行します:
    ```bash
    docker-compose down
    ```

## プロジェクト構成

```
calxy/
├── .github/
├── build.gradle.kts
├── docker-compose.yml
├── Dockerfile.dev
├── logs/
│   └── 2025-09-25.md
├── README.md
├── README.ja.md
├── requirements.txt
└── src/
    ├── main/
    │   ├── kotlin/
    │   └── resources/
    └── test/
        ├── kotlin/
        └── resources/
```
