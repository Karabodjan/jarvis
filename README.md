# J.A.R.V.I.S.

*Java Agent Response & Visual Interaction System*

Desktop dashboard built in Java/JavaFX that acts as a control tower for AI coding agents. Launch agents, monitor their execution in real time, and automatically merge their Pull Requests — all from a single interface.

---

## 📋 About
Orchestrate a fleet of Google Jules agents through a native JavaFX dashboard. This desktop "Control Tower" allows you to deploy AI agents across multiple GitHub repositories, monitor their reasoning in real-time, and automate the entire lifecycle—from bug detection to autonomous PR merging. Featuring real-time Discord alerts and integrated Text-to-Speech (TTS) for hands-free status updates.


## 🛠️ Stack

- **Language:** Java 21 (LTS)
- **UI Framework:** JavaFX 21
- **Build:** Maven
- **Persistence:** SQLite (via JDBC)
- **Config:** JSON (via Jackson)
- **Voice:** OS-native TTS (Windows SAPI / macOS `say`)
- **Notifications:** Discord Webhooks
- **Auto-merge:** GitHub REST API

## 🏗️ Architecture

The project follows the **MVVM** (Model-View-ViewModel) pattern:

- `model/` — plain data classes (Agent, Task, LogEntry)
- `viewmodel/` — presentation logic and observable properties
- `view/` — FXML layouts and JavaFX controllers
- `service/` — agent service interfaces and implementations (Mock, Jules)
- `repository/` — SQLite access layer
- `util/` — helpers (JSON loader, voice, Discord, GitHub)

##  Getting started

### Prerequisites

- JDK 21+ ([Eclipse Temurin](https://adoptium.net/) recommended)
- Maven 3.8+ (or use the included `mvnw` wrapper)

### Run

```bash
git clone https://github.com/TEU_USERNAME/jarvis.git
cd jarvis
./mvnw javafx:run
```

On Windows:

```bash
mvnw.cmd javafx:run
```

## 👤 Author

**Celso Barros** — Karabodjan 

