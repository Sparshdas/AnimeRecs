# AnimeRecs

A personal anime, manga, and manhwa recommendation app, built as a native Android app in Kotlin with Jetpack Compose. Inspired by [AniBrain](https://anibrain.ai), which inspired this project's similarity-based recommendation approach but is no longer available.

AnimeRecs connects to your [AniList](https://anilist.co) account and suggests titles based on a custom similarity score — not just "people who watched X also watched Y," but a breakdown of genre overlap, tag overlap, format match, and AniList's own community recommendations, with an extra boost for genres you've personally rated highly.

Built using [Google AI Studio](https://aistudio.google.com) for code generation, with manual edits, debugging, and iteration on top.

This is a personal, single-user project — not published to the Play Store, not intended for general distribution.

## Features

- **Smart recommendations** — custom scoring algorithm combining genre/tag overlap, format matching, community recommendations, and your own rating history
- **AniList integration** — OAuth login (no password stored), synced with your real AniList list
- **Multi-account support** — switch between multiple AniList accounts
- **Full list management** — browse Watching / Completed / Planning / On Hold / Dropped, update status and episode/chapter progress directly in the app
- **Discovery mode** — random title discovery
- **Adult content toggle** — opt-in, off by default
- **Sort & filter** — e.g. sort your Completed list by score

## Tech stack

- Kotlin + Jetpack Compose
- AniList GraphQL API (OAuth 2.0 Implicit Grant)
- Retrofit + Moshi
- Coil for image loading
- Built with Google AI Studio

## Status

Actively developed as a personal project. Built and tested locally via Android Studio; CI build (debug APK) also available via GitHub Actions.

## Disclaimer

Built for personal use only. Not affiliated with AniList, AniBrain, or Google.
