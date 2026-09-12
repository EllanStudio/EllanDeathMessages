# EllanDeathMessages

Component-based death messages for Ellan servers.

## Features

- Replaces `PlayerDeathEvent` death messages without relying on CMI string formatting.
- Preserves Adventure components, including CraftEngine translatable item names.
- Random message templates with MiniMessage colors and placeholders.
- Per-world muting, range filtering, ignored players, and anti-spam.
- Cross-server delivery through Redis pub/sub.
- The source server delivers immediately while remote servers receive the same component JSON.

## Commands

- `/ellandeath reload`
- `/ellandeath status`

Permission: `ellandeath.admin`

## Build

```bash
./gradlew test jar
```

The output JAR is created in `build/libs/`.
