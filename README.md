# Calxy

Calxy is a modern spreadsheet application designed to be a lightweight and fast alternative to Microsoft Excel and Access. It supports Python for scripting instead of VBA and is built to be cross-platform (Windows, Mac, Linux) with zero external dependencies for the end-user.

This repository contains the source code for the Calxy application.

## ✨ Features

- **Lightweight and Fast:** Engineered for efficiency.
- **Modern Scripting:** Use Python for macros and advanced cell functions.
- **Compatibility:** Reads and writes Excel (`.xlsx`) and CSV files.
- **Cross-Platform:** Runs on Windows, macOS, and Linux.
- **Zero-Dependency Install:** End-users only need to install the application itself.

## 開発方針 (Development Policy)

基本的にはDockerにて開発を行なっており、現在はLinuxOS用でビルドされます。macOSやWindows用のネイティブパッケージをビルドする場合は、それぞれのOS上で直接Gradleタスクを実行する必要があります。

## 🛠️ Development Environment

This project uses a Docker-based development environment to ensure consistency across all developers.

### Prerequisites

- [Docker](https://www.docker.com/products/docker-desktop/) must be installed and running.

### Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/toma4423/calxy.git
    cd calxy
    ```

2.  **Build and start the development container:**
    The initial setup has been completed. The development container is now running in the background. You can connect to it using Docker.

3.  **Accessing the container:**
    To open a shell inside the running container, execute:
    ```bash
    docker exec -it calxy-app-dev-1 bash
    ```
    Your project files are located in the `/app` directory inside the container.

4.  **Stopping the container:**
    To stop the container, run the following command from the `calxy` directory on your host machine:
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
├── requirements.txt
└── src/
    ├── main/
    │   ├── kotlin/
    │   └── resources/
    └── test/
        ├── kotlin/
        └── resources/
```

---
### 🤖 **Note for Gemini Agent**

To understand the project's setup history, including the extensive troubleshooting of the Docker environment and build scripts, please review the work logs located in the `/logs` directory. This will ensure a smooth handover and prevent repeating resolved issues.
---
