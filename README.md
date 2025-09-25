# Calxy

Calxy is a modern spreadsheet application designed to be a lightweight and fast alternative to Microsoft Excel and Access. It supports Python for scripting instead of VBA and is built to be cross-platform (Windows, Mac, Linux) with zero external dependencies for the end-user.

This repository contains the source code for the Calxy application.

## ✨ Features

- **Lightweight and Fast:** Engineered for efficiency.
- **Modern Scripting:** Use Python for macros and advanced cell functions.
- **Compatibility:** Reads and writes Excel (`.xlsx`) and CSV files.
- **Cross-Platform:** Runs on Windows, macOS, and Linux.
- **Zero-Dependency Install:** End-users only need to install the application itself.

## 🛠️ Development Policy

Development is primarily done via Docker. The build process currently produces a Linux executable. To build native packages for macOS or Windows, you will need to run the corresponding Gradle tasks directly on those operating systems.

## 🚀 Getting Started (Docker Environment)

This project uses a Docker-based development environment to ensure consistency.

### Prerequisites

- [Docker](https://www.docker.com/products/docker-desktop/) must be installed and running.

### Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/toma4423/calxy.git
    cd calxy
    ```

2.  **Build and start the container:**
    ```bash
    docker-compose up -d --build
    ```
    The initial setup is complete, and the development container will be running in the background.

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

## Project Structure

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

---
### 🤖 **Note for Gemini Agent**

To understand the project's setup history, including the extensive troubleshooting of the Docker environment and build scripts, please review the work logs located in the `/logs` directory. This will ensure a smooth handover and prevent repeating resolved issues.
---