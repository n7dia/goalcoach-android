# GoalCoach

GoalCoach is an Android application for creating, visualizing, and tracking personal goals.
The app combines clean UX, offline-first design, and a scalable cloud-ready architecture to support future AI-driven insights and analytics.


## Overview

GoalCoach allows users to create structured goals, associate them with visual inspiration (vision boards), and track progress over time.
The application is built using Jetpack Compose and follows modern Android architectural best practices, with a clear separation between UI, domain logic, and data layers.

The long-term vision is to evolve GoalCoach into a data- and AI-assisted goal coaching platform, using cloud services for synchronization, analytics, and personalization.


## Features

- Create, edit, and manage personal goals
- Categorize goals (e.g., career, fitness, finances)
- Visual goal boards with images selected from the web
- Offline-first local persistence
- UI built with Jetpack Compose
- Navigation architecture ready for feature expansion


## Navigation (Jetpack Compose)

The app uses Jetpack Compose Navigation with a single-activity architecture.

### Screens

- #### Home / Goals List
    - Displays all user goals
    - Entry point to goal creation and goal details
- #### Add / Edit Goal
    - Create or update a goal
    - Define title, category, type, priority, and effort level
- #### Goal Details
    - View detailed information about a specific goal
    - Update progress
- #### Vision Board
    - Displays inspirational images linked to goals


## Architecture

GoalCoach follows a MVVM architecture.

### Data Models

- #### Goal
    - Core model representing a user-defined objective
    - Includes metadata such as category, type, priority, and progress
- #### Supporting Models
    - Enums / sealed classes for goal types and categories
    - UI state models to represent loading, success, and error states

State is managed using ViewModels and exposed to the UI via state holders.

### Firebase Authentication

Firebase Authentication is used to manage user identity.

- Supports email/password authentication
- Authentication state is isolated from UI logic
- Advantage:
    - Secure per-user data access
    - Future multi-device synchronization

Firebase is used strictly for identity.


## Azure (Planned)

Azure services are planned to support backend capabilities beyond the client:

- #### Azure Storage / Data Layer
    - Cloud-backed storage for user data and analytics
    - Designed to complement local Room persistence
- #### Azure Data Pipeline
    - Enables aggregation of anonymized goal data
    - Supports future analytics and insights
- #### Azure AI Services
    - Personalized goal recommendations
    - Progress trend analysis
    - Intelligent nudges based on user behavior

The architecture intentionally separates mobile concerns from cloud concerns, allowing each layer to evolve independently.


## Tech Stack

- Language: Kotlin
- UI: Jetpack Compose, Material 3
- Architecture: MVVM
- Navigation: Jetpack Compose Navigation
- Persistence: Room (local)
- Image Loading: Coil
- Authentication: Firebase Auth
- Cloud / Data: Azure (data pipeline & AI services)


## Project Status

### 🚧 Active development

Planned enhancements include:

- Cloud synchronization
- Advanced analytics dashboards
- AI-powered goal insights
- User profiles and settings


## License

No License
