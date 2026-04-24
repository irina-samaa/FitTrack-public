# FitTrack

FitTrack is a JavaFX fitness tracking desktop app for logging workouts, monitoring health metrics, viewing progress trends, and managing workout reminders in one place.

## Features

- Dashboard with workout summary, BMI snapshot, streak tracking, reminders, and workload history
- Workout logging by body part and exercise type
- Support for strength, cardio, and endurance exercise entries
- Health page for updating weight and height with BMI feedback
- Progress page with weight history and moving-average charts
- Schedule page for one-time or repeating reminders

## Built With

- Java 17
- JavaFX 17
- Maven

## Demo Data

This project currently uses seeded in-memory data for a demo user and does not persist data to a database.

- Demo username: `admin`
- Demo password: `1234`

Note: the app currently auto-loads the demo user on startup in `Main.java`, so the main window opens directly. The login screen is still available after logging out.

## How To Clone

```powershell
git clone https://github.com/irina-samaa/FitTrack-public.git
cd FitTrack-public
```

## How To Run

### Prerequisites

- Git installed
- Java 17 installed
- Maven installed

### Start the app

```powershell
mvn clean javafx:run
```

If the dependencies are already installed locally, the app should open in a JavaFX window.

## Project Structure

```text
src/main/java/com/fittrack
|-- Main.java
|-- controller
|-- model
|-- service
`-- util

src/main/resources/com/fittrack
|-- main.fxml
|-- login.fxml
|-- dashboard.fxml
|-- workout.fxml
|-- schedule.fxml
|-- health.fxml
|-- progress.fxml
`-- styles.css

pom.xml
```

- `controller`: JavaFX controllers for each screen
- `model`: core fitness, reminder, and workout data models
- `service`: application service layer
- `util`: seeded demo datastore
- `resources`: FXML layouts and stylesheet

## Class Structure

The program follows a simple JavaFX + service-layer design. Controllers handle UI events, the service layer handles application logic, and the model layer stores fitness data.

```text
com.fittrack
|-- Main
|-- controller
|   |-- MainController
|   |-- LoginController
|   |-- DashboardController
|   |-- WorkoutController
|   |-- ScheduleController
|   |-- HealthController
|   `-- ProgressController
|-- service
|   `-- FitnessTrackerService
|-- util
|   `-- DataStore
`-- model
    |-- User
    |-- WorkoutSession
    |-- BodyPart
    |-- ExerciseType (enum)
    |-- Exercise (abstract)
    |   |-- StrengthExercise
    |   |-- CardioExercise
    |   `-- EnduranceExercise
    |-- SetRecord (abstract)
    |   |-- StrengthSetRecord
    |   |-- CardioSetRecord
    |   `-- EnduranceSetRecord
    |-- HealthMetrics
    |-- ProgressTracker
    |-- ReminderService
    |-- ReminderDisplayItem (interface)
    |-- Reminder
    `-- BodyPartInactivityAlert (record)
```

## Key Class Relationships

- `Main` is the JavaFX entry point and loads the main window.
- `MainController` switches between the dashboard, workout, schedule, health, and progress pages.
- All controllers use `FitnessTrackerService` as the main access point for business logic.
- `FitnessTrackerService` coordinates `DataStore`, `HealthMetrics`, and `ProgressTracker`.
- `DataStore` holds the demo user, seeded body parts, workout sessions, and `ReminderService`.
- `User` stores profile data, weight history, and workout history.
- `BodyPart` contains multiple `Exercise` objects.
- `Exercise` is an abstract parent class for `StrengthExercise`, `CardioExercise`, and `EnduranceExercise`.
- Each `Exercise` contains a list of `SetRecord` objects.
- `SetRecord` is an abstract parent class for `StrengthSetRecord`, `CardioSetRecord`, and `EnduranceSetRecord`.
- `WorkoutSession` groups completed exercises into one logged workout.
- `ReminderDisplayItem` is implemented by both `Reminder` and `BodyPartInactivityAlert`, so both can be shown in the reminder UI.

## Notes

- Workout sessions are created from the exercises and sets currently prepared in the workout screen.
- Weight progress is based on the user's recorded weight history.
- Reminder and workout data are reset when the app restarts because the current version uses in-memory storage.
