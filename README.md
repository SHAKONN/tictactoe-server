# Tic-Tac-Toe Server

Java TCP Socket Server for the Tic-Tac-Toe Android game.

## Deploy to Railway

1. Push this folder to a GitHub repository
2. Go to [railway.app](https://railway.app) → New Project → Deploy from GitHub Repo
3. Select this repository
4. In **Settings → Networking**, expose TCP port `5000`
5. Railway will give you a public domain like: `your-app.railway.app:PORT`

## Local run

```bash
javac *.java
java MainServer
```
