# 次世代表計算ソフトプロジェクト 仕様書 (v2.0 - 修正版)

## 概要

このドキュメントは、次世代の表計算ソフトウェア「Calxy」の開発仕様を定義します。当初の仕様書に含まれていたビルド関連の問題点を、実際の開発環境構築プロセスで得られた知見に基づき修正しています。

---

(中略：プロジェクト概要、技術仕様、機能仕様、ビジネスモデルは変更なし)

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