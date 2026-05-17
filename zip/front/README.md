# PolifyFront (Expo React Native)

## Dev Setup

Install deps:
- `npm install`

Run:
- `npm start`

## Backend URL (important for real phone testing)

Default backend URL logic is in [env.ts](/d:/Code_projects/Java/Polify/PolifyFront/src/config/env.ts):
- Android emulator: `http://10.0.2.2:8080`
- Otherwise: `http://localhost:8080`

When using Expo Go on a real phone (QR code), `localhost` points to the phone, not your PC.

Set `EXPO_PUBLIC_API_BASE_URL` to your PC LAN IP:
- Example: `http://192.168.0.10:8080`

You can set it in your shell before running Expo:
```bash
set EXPO_PUBLIC_API_BASE_URL=http://192.168.0.10:8080
npm start
```

## Current Screens (MVP)

- Auth: Login, Register
- Surveys: list, details
- Attempt runner: renders question types (TEXT/RADIO/CHECKBOX/SELECT/PRIORITY) with progress header + back/next and exit to surveys.

