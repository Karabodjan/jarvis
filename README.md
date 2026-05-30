# J.A.R.V.I.S.

*Java Agent Response & Visual Interaction System*

Desktop dashboard built in Java/JavaFX that acts as a control tower for AI coding agents. Launch agents, monitor their execution in real time, and automatically merge their Pull Requests — all from a single interface.

---

## About

Orchestrate AI coding agents through a native JavaFX dashboard. This desktop "Control Tower" deploys agents across GitHub repositories, monitors their execution in real time, and automates the entire lifecycle — from task assignment to autonomous PR merging. Featuring real-time Discord alerts and integrated Text-to-Speech (TTS) for hands-free status updates.

---

##  Stack

| Component | Technology |
|---|---|
| Language | Java 21 LTS |
| UI Framework | JavaFX 21 |
| Build | Maven (with `mvnw` wrapper) |
| Persistence | SQLite via JDBC (Xerial 3.53) |
| Config & serialisation | JSON via Jackson 2.17.2 |
| Voice | OS-native TTS (Windows SAPI / macOS `say`) |
| Notifications | Discord Webhooks |
| Auto-merge | GitHub REST API |

---

##  Architecture

The project follows the **MVVM** (Model-View-ViewModel) pattern with strict separation of concerns.

```
src/main/java/fr/karabodjan/jarvis/
├── model/          — Immutable data classes (Agent) and runtime records (AgentResult, PersistedRun)
├── viewmodel/      — Presentation logic and JavaFX observable properties
├── view/           — FXML controllers (no business logic)
├── service/        — IAgentService contract + MockAgentService / AgentTask implementations
├── repository/     — SQLite access layer (RunHistoryRepository, SqliteRunRepository)
├── integration/    — OS and external integrations (VoiceService, DiscordNotifier, GitHubService)
└── util/           — Configuration loading (JsonLoader, ConfigLoader, JarvisConfig)
```

**Key design principles:**

- **No UI freeze** — all long-running operations run on daemon background threads via JavaFX `Task<T>`. Every UI update from a background thread uses `Platform.runLater()`.
- **Dependency inversion** — `IAgentService` decouples the ViewModel from any concrete agent implementation. Swapping Mock for a real API requires zero changes outside the composition root.
- **Immutable model** — `Agent` is fully immutable (all fields `final`). Runtime state is isolated in `model/run/`, never mixed into the domain model.
- **Configuration over code** — agents and integrations are configured via JSON files, not recompilation.

---

## ✨ Features

### Agent management
- Load agents dynamically from `agents.json` — add or remove agents without touching Java code
- Launch multiple agents simultaneously, each on its own background thread
- Cancel a running agent at any time
- Real-time log console per agent, updated live during execution
- Status lifecycle: `IDLE → RUNNING → COMPLETED / FAILED / CANCELLED`

### History & persistence
- Full run history persisted in a local SQLite database (`jarvis.db`)
- History screen with a filterable `TableView` (by agent or status)
- Export run history to CSV with one click

### Integrations
- **TTS Voice** — OS-native speech synthesis (Windows: SAPI via PowerShell; macOS: `say`). Zero dependencies, zero cost, works offline. JARVIS announces every key event.
- **Discord notifications** — colour-coded embedded messages sent to a webhook channel on every agent event (launched, completed, failed, merged).
- **GitHub Auto-merge** — when an agent completes and submits a PR, JARVIS can merge it automatically via the GitHub REST API (`PUT /pulls/{n}/merge`). Toggle **ON / OFF** in the toolbar at runtime without restarting.

### Demo safety
- **Mock mode** — simulates a full 5-phase agent run locally (no API account required). Reliable fallback for live demos if any external API is unavailable.

---

## ⚙️ Configuration

### `agents.json`

Location: `src/main/resources/fr/karabodjan/jarvis/agents.json`

```json
{
  "agents": [
    {
      "id": "a1",
      "name": "BugFixer Alpha",
      "repoUrl": "https://github.com/your-user/your-repo",
      "taskType": "bug_fix"
    }
  ]
}
```

### `config.json`

Location: project root (same level as `pom.xml`).

> ⚠️ This file is listed in `.gitignore` and must **never** be committed. It contains secrets.

```json
{
  "discordWebhookUrl": "https://discord.com/api/webhooks/...",
  "githubToken": "ghp_...",
  "autoMergeEnabled": true
}
```

| Field | Required | Description |
|---|---|---|
| `discordWebhookUrl` | No | Discord channel webhook URL. Notifications are silently skipped if absent. |
| `githubToken` | No | GitHub Personal Access Token with `repo` scope. Auto-merge is skipped if absent. |
| `autoMergeEnabled` | No | `true` / `false`. Can also be toggled live in the toolbar. |

The application starts normally with no `config.json` — voice, Discord and auto-merge are simply disabled. Only mock mode requires no configuration at all.

---

## 🚀 Getting Started

### Prerequisites

- JDK 21+ ([Eclipse Temurin](https://adoptium.net/) recommended)
- Maven 3.8+ (or use the included `mvnw` wrapper — no separate Maven install needed)

### Run

```bash
git clone https://github.com/Karabodjan/jarvis.git
cd jarvis
./mvnw javafx:run        # Linux / macOS
mvnw.cmd javafx:run      # Windows
```

No configuration is required to run in mock mode. To enable Discord notifications and GitHub auto-merge, create `config.json` as described above before launching.

---

## 👤 Author

**Celso Barros** — [Karabodjan](https://github.com/Karabodjan)

