# 📚 MangaVerse

MangaVerse is a sleek and modern Android app that lets users explore, search, and read manga content fetched from a RapidAPI server. It supports offline viewing via local caching with Room DB, features a CameraX-powered screen, and integrates a real-time face detector using MediaPipe. User preferences and login states are handled using Jetpack DataStore.

---

## ✨ Features

- 🔄 **Paginated Manga Fetching**  
  Efficient and smooth pagination from a [RapidAPI](https://rapidapi.com/sagararofie/api/mangaverse-api/playground/apiendpoint_94a3a9ab-6549-4c86-b8dc-e7eb088c2228) manga API.

- 💾 **Offline Support**  
  Caching with **Room Database** enables offline access to previously fetched manga.

- 📸 **CameraX Integration**  
  Built-in camera screen using **CameraX API** with a smooth preview and capture experience.

- 🧠 **Real-time Face Detection**  
  Face detector powered by **MediaPipe**, integrated seamlessly into the camera screen.

- 🔐 **Login State Management**  
  User preferences and login states saved securely using **Jetpack DataStore**.

---

## 🧰 Tech Stack

| Layer                 | Technology Used              |
|-----------------------|------------------------------|
| UI                    | Jetpack Compose              |
| Network               | Retrofit + RapidAPI          |
| Local Cache           | Room Database                |
| Offline Support       | Room + Repository Pattern    |
| Face Detection        | MediaPipe                    |
| Camera                | CameraX API                  |
| Preferences           | Jetpack DataStore            |
| Architecture          | MVVM                         |
| Dependency Injection  | koin                         |
| Pagination            | Paging 3                     |

---

## 📸 Screenshots

| Home | Description | Camera | Face Detection |
|------|-------------|--------|----------------|
| ![Home](https://github.com/user-attachments/assets/8b575e40-b9b3-414d-b569-3ea262b35e9c) | ![Description Screen](https://github.com/user-attachments/assets/c0b2783d-0048-4352-adf1-12db427af948) | ![Sign In Screen](https://github.com/user-attachments/assets/f2998e07-b774-46a3-9150-7f39f2d7255b) | ![Face Detection](https://github.com/user-attachments/assets/07c4fc71-8354-4529-9887-b29886f6e311) |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (Arctic Fox or later)
- Kotlin 1.6+
- Gradle 7.0+
- Internet connection (for API access)

### Clone the Repository

```bash
https://github.com/nishchaymakkar/MangaApp.git
cd manga-verse
