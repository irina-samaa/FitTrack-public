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
- JDK 17 installed
- Maven installed

### 1. Open the project folder

If you have not cloned the repo yet, follow the `How To Clone` section above first.

Then open a terminal inside the `FitTrack-public` folder.

### 2. Check Java and Maven

Open a terminal in the project folder and run:

```powershell
java -version
mvn -version
```

You should see:

- Java version `17`
- A Maven version output instead of a "command not found" or "not recognized" error

If `java -version` fails, install JDK 17 and make sure Java is added to your `PATH`.

If `mvn -version` fails, install Maven and make sure Maven's `bin` folder is added to your `PATH`.

### 3. Run the app from the terminal

```powershell
mvn clean javafx:run
```

What this command does:

- `clean` removes old compiled files from previous runs
- `javafx:run` builds the project and launches the JavaFX app

Notes:

- The first run on a new machine may take a little longer because Maven needs to download dependencies into the user's local `.m2` cache.
- You do not need a project-local `.m2` folder in this repo.
- If everything is set up correctly, a JavaFX window should open after Maven finishes building.

### 4. Run the app in VS Code

- Open the folder in VS Code
- Make sure the Java extension pack is installed
- Let Maven import the project dependencies
- Wait until the Java language server finishes loading the project
- Open `pom.xml` once if VS Code has not auto-detected Maven yet
- Run the `Launch FitTrack (JavaFX)` launch configuration from the Run and Debug panel

If VS Code asks to import or trust the Maven project, accept it.

### 5. Common setup issues

#### `mvn` is not recognized

This means Maven is not installed or not added to `PATH`.

Fix:

- Install Maven
- Add Maven's `bin` directory to your system `PATH`
- Reopen the terminal and run `mvn -version` again

#### `java` is not recognized

This means Java is not installed or not added to `PATH`.

Fix:

- Install JDK 17
- Add the JDK `bin` directory to your system `PATH`
- Reopen the terminal and run `java -version` again

#### The Java version is not 17

This project is configured for Java 17 in `pom.xml`.

Fix:

- Switch your machine or IDE to JDK 17
- Then rerun `java -version`

#### The first run takes a long time

This is normal on a new machine. Maven may need to download JavaFX and plugin dependencies before the app can start.

#### The app builds but no window appears in VS Code

Try running from the terminal first:

```powershell
mvn clean javafx:run
```

If that works, the issue is usually VS Code's Java configuration rather than the project itself.

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
