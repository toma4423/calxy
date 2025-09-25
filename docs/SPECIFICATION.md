# 次世代表計算ソフトプロジェクト 仕様書 (v2.0 - 修正版)

このドキュメントは、次世代の表計算ソフトウェア「Calxy」の開発仕様を定義します。当初の仕様書に含まれていたビルド関連の問題点を、実際の開発環境構築プロセスで得られた知見に基づき修正しています。

---

## 1. プロジェクト概要
### 1.1 目的
Microsoft ExcelおよびAccessの代替となる現代的な表計算ソフトウェアの開発。従来のVBAに代わり、Pythonでマクロを記述可能にし、軽量で高速な動作を実現する。利用者はアプリケーションをインストールするだけで、PythonやJVMの追加インストール無しに全機能を利用できることを目指す。
### 1.2 コアコンセプト
- **軽量・高速:** ハードウェアスペックに頼らない根本的な効率性。
- **現代的言語サポート:** VBAではなくPythonでマクロ記述。
- **完全な互換性:** Excel/Accessファイルの読み書き対応。
- **クロスプラットフォーム:** Windows, Mac, Linux全対応。
- **ゼロ依存インストール:** ユーザーはアプリケーション本体以外のインストールが一切不要。

### 1.3 ターゲットユーザー
- **プログラマー・データサイエンティスト:** 現代的言語でのデータ処理を求める。
- **Excel疲れの企業ユーザー:** 軽量で高速な代替ソフトを求める。
- **中小企業:** ライセンス費用削減とカスタマイズ性を重視。
- **教育機関・研究機関:** オープンソース志向の組織。

### 1.4 プロジェクトの不変原則 (Project Constitution)
開発に着手するにあたり、本プロジェクトの根幹を成し、将来にわたって変更されることのない「憲法」として以下の5つの原則を定める。
- **ユーザー体験の最優先:** ワンクリック・ゼロ依存
- **技術的根幹:** Kotlin + GraalVM Python
- **柔軟な拡張性:** 二言語マクロシステムの維持
- **パフォーマンスと機能性の両立:** 階層化された計算エンジン
- **ビジネスの持続性:** フリーミアムモデル

---

## 2. 技術仕様
### 2.1 確定アーキテクチャ: Kotlin + GraalVM Python
アプリケーションのコアロジックとUIはKotlinで構築し、ユーザーが記述するマクロや高度なセル計算は、アプリケーションに完全に内蔵されたGraalVM Python環境で実行する。

### 2.2 コア技術スタック
- **メイン言語:** Kotlin (JDK 17)
- **UIフレームワーク:** Jetpack Compose for Desktop
- **Python統合:** GraalVM Polyglot API (Python 3.10+)
- **ファイルI/O:** Apache POI (Excel互換)
- **データベース:** Exposed (Kotlin製ORM), JDBC
- **ネットワーク:** Ktor Client

---

## 3. 機能仕様
### 3.1 MVP (Minimum Viable Product) 機能
- **スプレッドシート基本機能:** 104万行×1万6千列、100種類以上の基本関数、基本書式設定、基本チャート作成。
- **マクロシステム:** Kotlin/Pythonでの記述・保存・実行、基本的なスプレッドシートAPI。
- **ファイルI/O:** .xlsx, CSV, TSVの読み書き、独自の高速バイナリ形式。

### 3.2 フル機能
- **データベース統合:** ビジュアルクエリビルダー、主要DBへの接続、インポート・エクスポート。
- **協調作業機能 (有償):** リアルタイム同時編集、コメント、バージョン管理。
- **Access代替機能:** GUIでのテーブル設計、カスタムフォーム、レポートデザイナー。

---

## 5. 開発計画 (修正版)

### 5.3 開発環境・CI/CD (修正・動作確認済み)

#### 5.3.1 開発環境: Docker + GraalVM + uv

全開発者が完全に同一の環境で開発を行うため、Dockerコンテナを必須とする。コンテナは安定した `debian:bullseye-slim` をベースとし、GraalVM、Python、Gradle、uvを段階的にインストールする。

**Dockerfile.dev (最終版)**

```dockerfile
# Stage 1: Build the development environment from a stable base
FROM debian:bullseye-slim as builder

# Set environment variables
ENV JAVA_HOME=/opt/graalvm
ENV GRADLE_HOME=/opt/gradle
ENV UV_HOME=/opt/uv
ENV GRADLE_VERSION=8.7
ENV PATH="$JAVA_HOME/bin:$GRADLE_HOME/bin:$UV_HOME:$PATH"

# Install base dependencies
RUN apt-get update && apt-get install -y \
    curl \
    unzip \
    build-essential \
    libffi-dev \
    zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*

# Install GraalVM CE for Java 17 from GitHub Releases
RUN curl -LsSf https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.3.1/graalvm-ce-java17-linux-aarch64-22.3.1.tar.gz -o graalvm.tar.gz && \
    mkdir -p $JAVA_HOME && \
    tar -xzf graalvm.tar.gz -C $JAVA_HOME --strip-components=1 && \
    rm graalvm.tar.gz

# Install Python language into GraalVM
RUN gu install python

# Install Gradle
RUN curl -LsSf https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle.zip && \
    unzip gradle.zip -d /opt && \
    mv /opt/gradle-${GRADLE_VERSION} $GRADLE_HOME && \
    rm gradle.zip

# Install uv
RUN curl -LsSf https://astral.sh/uv/install.sh | env UV_INSTALL_DIR=$UV_HOME sh

# Final image for the development environment
FROM debian:bullseye-slim

# Copy the built environment from the builder stage
COPY --from=builder /opt/graalvm /opt/graalvm
COPY --from=builder /opt/gradle /opt/gradle
COPY --from=builder /opt/uv /opt/uv

# Set environment variables for the final image
ENV JAVA_HOME=/opt/graalvm
ENV GRADLE_HOME=/opt/gradle
ENV UV_HOME=/opt/uv
ENV PATH="$JAVA_HOME/bin:$GRADLE_HOME/bin:$UV_HOME:$PATH"

# Set up the working directory
WORKDIR /app

# Install runtime dependencies for UI and packaging
RUN apt-get update && apt-get install -y libffi-dev zlib1g libgl1 libfontconfig1 binutils fakeroot && rm -rf /var/lib/apt/lists/*

# Copy Python dependencies file
COPY requirements.txt .

# Install Python libraries using uv
RUN uv venv python_env && \
    uv pip install --keyring-provider=disabled --python python_env/bin/python -r requirements.txt

# Add the venv to the PATH
ENV PATH="/app/python_env/bin:$PATH"

# Expose ports
EXPOSE 8080 5005

# Keep the container running
CMD ["bash"]
```

**docker-compose.yml (最終版)**

```yaml
version: '3.8'

services:
  app-dev:
    build:
      context: .
      dockerfile: Dockerfile.dev
    volumes:
      - .:/app
      - gradle-cache:/root/.gradle
    ports:
      - "8081:8080" # Host port 8080 was in use, changed to 8081
      - "5005:5005" # Debug port
    tty: true # Keep container running
    environment:
      - GRADLE_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:5005

volumes:
  gradle-cache:
```

### 5.4 プロジェクト構成とビルド設定 (修正・動作確認済み)

#### 5.4.2 ビルド設定: build.gradle.kts (最終版)

```kotlin
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.compose") version "1.6.2"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "com.spreadsheet"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    flatDir {
        val graalVmHome = System.getenv("JAVA_HOME")
        dirs(
            "$graalVmHome/lib/polyglot",
            "$graalVmHome/languages/python",
            "$graalVmHome/lib/truffle"
        )
    }
}

// === 依存関係の定義 ===
dependencies {
    // UI
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    // Kotlin Core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Python Integration (GraalVM)
    implementation(group = "org.graalvm.polyglot", name = "polyglot-native-api", version = "local")
    implementation(group = "org.graalvm.python", name = "graalpython", version = "local")
    implementation(group = "org.graalvm.truffle", name = "truffle-api", version = "local")

    // File I/O
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.mockk:mockk:1.13.10")
}

// === 配布パッケージ生成設定 ===
compose.desktop {
    application {
        mainClass = "com.spreadsheet.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SpreadsheetNext"
            packageVersion = "1.0.0"
            description = "A modern spreadsheet application"
            vendor = "SpreadsheetNext Corp"

            windows {
                menuGroup = "SpreadsheetNext"
                shortcut = true
                // iconFile.set(project.file("src/main/resources/icons/icon.ico"))
            }
            macOS {
                bundleID = "com.spreadsheet.next"
                // iconFile.set(project.file("src/main/resources/icons/icon.icns"))
            }
            linux {
                // iconFile.set(project.file("src/main/resources/icons/icon.png"))
            }
        }
    }
}

// === コード品質ツールの設定 ===
ktlint {
    version.set("1.2.1")
    outputToConsole.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    }
}

detekt {
    config.setFrom(files("detekt-config.yml"))
    buildUponDefaultConfig = true
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "17"
}

// === テストタスクの設定 ===
tasks.test {
    useJUnitPlatform()
}
```