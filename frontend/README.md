# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some Oxlint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the Oxlint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and Oxlint's TypeScript related rules in your project.

**Update root `README.md`:**
```markdown
#  ByteSwarm — Browser-Based Distributed Compute Grid

Turn thousands of idle browser tabs across an enterprise into a supercomputer.

##  Concept
An analyst uploads a heavy compute job (e.g., 10M records) → Java (Netty) chunks the data → Streams chunks via WebSocket to 50+ React dashboards → Browsers compute silently in Web Workers → Java stitches results back together.

## 👥 Team
| Member | Role |
|--------|------|
| **Bhanu** | Backend Core — Netty Server, Dispatcher, Metrics API |
| **Balaji** | Backend Logic — Chunking, Job Manager, Testing |
| **Mayank** | Frontend Core — React, WebSocket hook, Web Workers |
| **Mansi** | Frontend UI/UX — Dashboard, Grid, Progress, Polish |

##  Tech Stack
- **Backend:** Java 21 · Netty 4 · Jackson · Maven · JUnit 5
- **Frontend:** React 18 · Vite · HTML5 Web Workers API

##  Quick Start

### 1. Start Backend
```powershell
cd backend
mvn clean install
mvn exec:java