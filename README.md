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
src/main/resources/com/fittrack
pom.xml
```

- `controller`: JavaFX controllers for each screen
- `model`: core fitness, reminder, and workout data models
- `service`: application service layer
- `util`: seeded demo datastore
- `resources`: FXML layouts and stylesheet

## Notes

- Workout sessions are created from the exercises and sets currently prepared in the workout screen.
- Weight progress is based on the user's recorded weight history.
- Reminder and workout data are reset when the app restarts because the current version uses in-memory storage.
