package com.fittrack.util;

import com.fittrack.model.BodyPart;
import com.fittrack.model.Exercise;
import com.fittrack.model.ExerciseType;
import com.fittrack.model.Reminder;
import com.fittrack.model.User;
import com.fittrack.model.WorkoutSession;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class SQLiteRepository {
    private static final Path DB_PATH = Paths.get("fittrack.db").toAbsolutePath();
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    public SQLiteRepository() {
        initialize();
    }

    public UserCredentials findUserCredentials(String username) {
        String sql = "SELECT username, password FROM users WHERE username = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new UserCredentials(
                    resultSet.getString("username"),
                    resultSet.getString("password")
                );
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find user credentials.", e);
        }
    }

    public boolean createUser(String username, String password, double weight, double height) {
        String sql = "INSERT INTO users(username, password, weight, height) VALUES (?, ?, ?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setDouble(3, weight);
            statement.setDouble(4, height);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (isUniqueConstraintViolation(e)) {
                return false;
            }
            throw new IllegalStateException("Failed to create user.", e);
        }
    }

    public LoadedUserData loadUserData(String username) {
        try (Connection connection = openConnection()) {
            long userId = findUserId(connection, username);
            User user = loadUser(connection, userId);
            loadWeightHistory(connection, userId, user);
            ArrayList<BodyPart> bodyParts = loadBodyParts(connection, userId);
            ArrayList<WorkoutSession> sessions = loadSessions(connection, userId);
            ArrayList<Reminder> reminders = loadReminders(connection, userId);
            return new LoadedUserData(user, bodyParts, sessions, reminders);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load user data.", e);
        }
    }

    public void saveUserProfile(User user) {
        String sql = "UPDATE users SET weight = ?, height = ? WHERE username = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, user.getWeight());
            statement.setDouble(2, user.getHeight());
            statement.setString(3, user.getUsername());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save user profile.", e);
        }
    }

    public void saveWeightHistory(User user) {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            long userId = findUserId(connection, user.getUsername());
            deleteByUserId(connection, "weight_history", userId);

            String insertSql = "INSERT INTO weight_history(user_id, record_order, weight, record_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                List<Double> history = user.getWeightHistory();
                List<LocalDate> dates = user.getWeightHistoryDates();
                for (int i = 0; i < history.size(); i++) {
                    statement.setLong(1, userId);
                    statement.setInt(2, i);
                    statement.setDouble(3, history.get(i));
                    statement.setString(4, dates.get(i).toString());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save weight history.", e);
        }
    }

    public void saveWorkoutDraft(String username, List<BodyPart> bodyParts) {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            long userId = findUserId(connection, username);
            deleteWorkoutDraft(connection, userId);

            String bodyPartSql = "INSERT INTO body_parts(user_id, name) VALUES (?, ?)";
            String exerciseSql = "INSERT INTO exercises(body_part_id, name, type) VALUES (?, ?, ?)";
            String setSql = "INSERT INTO exercise_sets(exercise_id, metric1, metric2) VALUES (?, ?, ?)";

            try (PreparedStatement bodyPartStatement = connection.prepareStatement(bodyPartSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement exerciseStatement = connection.prepareStatement(exerciseSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement setStatement = connection.prepareStatement(setSql)) {
                for (BodyPart bodyPart : bodyParts) {
                    long bodyPartId = insertBodyPart(bodyPartStatement, userId, bodyPart.getName());
                    for (Exercise exercise : bodyPart.getExercises()) {
                        long exerciseId = insertExercise(exerciseStatement, bodyPartId, exercise);
                        insertExerciseSets(setStatement, exerciseId, exercise);
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save workout draft.", e);
        }
    }

    public void saveSessions(String username, List<WorkoutSession> sessions) {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            long userId = findUserId(connection, username);
            deleteSessions(connection, userId);

            String sessionSql = "INSERT INTO workout_sessions(user_id, session_date, session_name) VALUES (?, ?, ?)";
            String exerciseSql = "INSERT INTO session_exercises(session_id, name, type, body_part_name) VALUES (?, ?, ?, ?)";
            String setSql = "INSERT INTO session_sets(session_exercise_id, metric1, metric2) VALUES (?, ?, ?)";

            try (PreparedStatement sessionStatement = connection.prepareStatement(sessionSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement exerciseStatement = connection.prepareStatement(exerciseSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement setStatement = connection.prepareStatement(setSql)) {
                for (WorkoutSession session : sessions) {
                    long sessionId = insertSession(sessionStatement, userId, session);
                    for (Exercise exercise : session.getExercises()) {
                        long sessionExerciseId = insertSessionExercise(exerciseStatement, sessionId, exercise);
                        insertExerciseSets(setStatement, sessionExerciseId, exercise);
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save sessions.", e);
        }
    }

    public void saveReminders(String username, List<Reminder> reminders) {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            long userId = findUserId(connection, username);
            deleteByUserId(connection, "reminders", userId);

            String sql = "INSERT INTO reminders(user_id, label, scheduled_time, repeat_interval_days, note) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Reminder reminder : reminders) {
                    statement.setLong(1, userId);
                    statement.setString(2, reminder.getBodyPartName());
                    statement.setString(3, reminder.getScheduledTime().toString());
                    statement.setInt(4, reminder.getIntervalDays());
                    statement.setNull(5, java.sql.Types.VARCHAR);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save reminders.", e);
        }
    }

    private void initialize() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    weight REAL NOT NULL,
                    height REAL NOT NULL
                )
                """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS weight_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    record_order INTEGER NOT NULL,
                    weight REAL NOT NULL,
                    record_date TEXT NOT NULL,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """);
            ensureWeightHistoryDateColumn(connection);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS body_parts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS exercises (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    body_part_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    FOREIGN KEY(body_part_id) REFERENCES body_parts(id) ON DELETE CASCADE
                )
                """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS exercise_sets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    exercise_id INTEGER NOT NULL,
                    metric1 INTEGER NOT NULL,
                    metric2 REAL NOT NULL,
                    FOREIGN KEY(exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
                )
                """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS workout_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    session_date TEXT NOT NULL,
                    session_name TEXT NOT NULL,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS session_exercises (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    body_part_name TEXT,
                    FOREIGN KEY(session_id) REFERENCES workout_sessions(id) ON DELETE CASCADE
                )
                """);
            ensureSessionExerciseBodyPartNameColumn(connection);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS session_sets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_exercise_id INTEGER NOT NULL,
                    metric1 INTEGER NOT NULL,
                    metric2 REAL NOT NULL,
                    FOREIGN KEY(session_exercise_id) REFERENCES session_exercises(id) ON DELETE CASCADE
                )
                """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS reminders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    label TEXT NOT NULL,
                    scheduled_time TEXT NOT NULL,
                    repeat_interval_days INTEGER,
                    note TEXT,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """);
            seedIfEmpty(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize SQLite database.", e);
        }
    }

    private void ensureWeightHistoryDateColumn(Connection connection) throws SQLException {
        boolean hasRecordDate = false;
        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(weight_history)")) {
            while (columns.next()) {
                if ("record_date".equalsIgnoreCase(columns.getString("name"))) {
                    hasRecordDate = true;
                    break;
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            if (!hasRecordDate) {
                statement.execute("ALTER TABLE weight_history ADD COLUMN record_date TEXT");
            }
            statement.execute("""
                UPDATE weight_history
                SET record_date = date('now', '-' || (
                    SELECT MAX(history.record_order) - weight_history.record_order
                    FROM weight_history history
                    WHERE history.user_id = weight_history.user_id
                ) || ' days')
                WHERE record_date IS NULL OR record_date = ''
                """);
        }
    }

    private void ensureSessionExerciseBodyPartNameColumn(Connection connection) throws SQLException {
        boolean hasBodyPartName = false;
        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(session_exercises)")) {
            while (columns.next()) {
                if ("body_part_name".equalsIgnoreCase(columns.getString("name"))) {
                    hasBodyPartName = true;
                    break;
                }
            }
        }

        if (!hasBodyPartName) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE session_exercises ADD COLUMN body_part_name TEXT");
            }
        }
    }

    private void seedIfEmpty(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return;
            }
        }

        connection.setAutoCommit(false);
        long adminId = insertUser(connection, "admin", "1234", 70.0, 175.0);
        insertWeightHistory(connection, adminId, List.of(68.0, 69.0, 70.0, 70.5, 71.0, 70.0, 69.5));
        seedAdminDraft(connection, adminId);
        seedAdminSessions(connection, adminId);
        seedAdminReminders(connection, adminId);
        insertUser(connection, "testuser", "1234", 70.0, 170.0);
        connection.commit();
        connection.setAutoCommit(true);
    }

    private void seedAdminDraft(Connection connection, long adminId) throws SQLException {
        BodyPart chest = new BodyPart("Chest");
        chest.createExercise("Bench Press", ExerciseType.STRENGTH).addSet(8, 60);
        chest.createExercise("Push Up", ExerciseType.STRENGTH).addSet(20, 0);
        chest.createExercise("Chest Fly", ExerciseType.STRENGTH).addSet(12, 15);

        BodyPart legs = new BodyPart("Legs");
        legs.createExercise("Squat", ExerciseType.STRENGTH).addSet(6, 90);
        legs.createExercise("Treadmill Run", ExerciseType.CARDIO).addSet(20, 3.2);

        BodyPart back = new BodyPart("Back");
        back.createExercise("Deadlift", ExerciseType.STRENGTH).addSet(5, 100);
        back.createExercise("Rowing Machine", ExerciseType.ENDURANCE).addSet(18, 145);

        String bodyPartSql = "INSERT INTO body_parts(user_id, name) VALUES (?, ?)";
        String exerciseSql = "INSERT INTO exercises(body_part_id, name, type) VALUES (?, ?, ?)";
        String setSql = "INSERT INTO exercise_sets(exercise_id, metric1, metric2) VALUES (?, ?, ?)";

        try (PreparedStatement bodyPartStatement = connection.prepareStatement(bodyPartSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement exerciseStatement = connection.prepareStatement(exerciseSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement setStatement = connection.prepareStatement(setSql)) {
            for (BodyPart bodyPart : List.of(chest, legs, back)) {
                long bodyPartId = insertBodyPart(bodyPartStatement, adminId, bodyPart.getName());
                for (Exercise exercise : bodyPart.getExercises()) {
                    long exerciseId = insertExercise(exerciseStatement, bodyPartId, exercise);
                    insertExerciseSets(setStatement, exerciseId, exercise);
                }
            }
        }
    }

    private void seedAdminSessions(Connection connection, long adminId) throws SQLException {
        WorkoutSession session = new WorkoutSession(LocalDate.now().minusDays(1), "Upper Body Session");

        BodyPart chest = new BodyPart("Chest");
        chest.createExercise("Bench Press", ExerciseType.STRENGTH).addSet(8, 60);
        chest.createExercise("Push Up", ExerciseType.STRENGTH).addSet(20, 0);
        chest.createExercise("Chest Fly", ExerciseType.STRENGTH).addSet(12, 15);
        for (Exercise exercise : chest.getExercises()) {
            session.addExercise(exercise.copy());
        }

        BodyPart back = new BodyPart("Back");
        back.createExercise("Deadlift", ExerciseType.STRENGTH).addSet(5, 100);
        back.createExercise("Rowing Machine", ExerciseType.ENDURANCE).addSet(18, 145);
        for (Exercise exercise : back.getExercises()) {
            session.addExercise(exercise.copy());
        }

        String sessionSql = "INSERT INTO workout_sessions(user_id, session_date, session_name) VALUES (?, ?, ?)";
        String exerciseSql = "INSERT INTO session_exercises(session_id, name, type, body_part_name) VALUES (?, ?, ?, ?)";
        String setSql = "INSERT INTO session_sets(session_exercise_id, metric1, metric2) VALUES (?, ?, ?)";

        try (PreparedStatement sessionStatement = connection.prepareStatement(sessionSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement exerciseStatement = connection.prepareStatement(exerciseSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement setStatement = connection.prepareStatement(setSql)) {
            long sessionId = insertSession(sessionStatement, adminId, session);
            for (Exercise exercise : session.getExercises()) {
                long sessionExerciseId = insertSessionExercise(exerciseStatement, sessionId, exercise);
                insertExerciseSets(setStatement, sessionExerciseId, exercise);
            }
        }
    }

    private void seedAdminReminders(Connection connection, long adminId) throws SQLException {
        String sql = "INSERT INTO reminders(user_id, label, scheduled_time, repeat_interval_days, note) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, adminId);
            statement.setString(2, "Chest");
            statement.setString(3, LocalDateTime.now().plusDays(5).toString());
            statement.setInt(4, 5);
            statement.setNull(5, java.sql.Types.VARCHAR);
            statement.addBatch();

            statement.setLong(1, adminId);
            statement.setString(2, "Back");
            statement.setString(3, LocalDateTime.now().plusDays(5).toString());
            statement.setInt(4, 5);
            statement.setNull(5, java.sql.Types.VARCHAR);
            statement.addBatch();

            statement.executeBatch();
        }
    }

    private User loadUser(Connection connection, long userId) throws SQLException {
        String sql = "SELECT username, password, weight, height FROM users WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("User not found for id " + userId);
                }
                return new User(
                    resultSet.getString("username"),
                    resultSet.getString("password"),
                    resultSet.getDouble("weight"),
                    resultSet.getDouble("height")
                );
            }
        }
    }

    private void loadWeightHistory(Connection connection, long userId, User user) throws SQLException {
        String sql = "SELECT weight, record_date FROM weight_history WHERE user_id = ? ORDER BY record_order ASC, id ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Double> history = new ArrayList<>();
                List<LocalDate> dates = new ArrayList<>();
                while (resultSet.next()) {
                    history.add(resultSet.getDouble("weight"));
                    dates.add(LocalDate.parse(resultSet.getString("record_date")));
                }
                if (!history.isEmpty()) {
                    user.seedWeightHistory(history, dates);
                }
            }
        }
    }

    private ArrayList<BodyPart> loadBodyParts(Connection connection, long userId) throws SQLException {
        ArrayList<BodyPart> result = new ArrayList<>();
        String bodyPartSql = "SELECT id, name FROM body_parts WHERE user_id = ? ORDER BY id ASC";
        String exerciseSql = "SELECT id, name, type FROM exercises WHERE body_part_id = ? ORDER BY id ASC";
        String setSql = "SELECT metric1, metric2 FROM exercise_sets WHERE exercise_id = ? ORDER BY id ASC";

        try (PreparedStatement bodyPartStatement = connection.prepareStatement(bodyPartSql);
             PreparedStatement exerciseStatement = connection.prepareStatement(exerciseSql);
             PreparedStatement setStatement = connection.prepareStatement(setSql)) {
            bodyPartStatement.setLong(1, userId);
            try (ResultSet bodyPartRows = bodyPartStatement.executeQuery()) {
                while (bodyPartRows.next()) {
                    BodyPart bodyPart = new BodyPart(bodyPartRows.getString("name"));
                    exerciseStatement.setLong(1, bodyPartRows.getLong("id"));
                    try (ResultSet exerciseRows = exerciseStatement.executeQuery()) {
                        while (exerciseRows.next()) {
                            Exercise exercise = bodyPart.createExercise(
                                exerciseRows.getString("name"),
                                ExerciseType.fromDisplayName(exerciseRows.getString("type"))
                            );
                            setStatement.setLong(1, exerciseRows.getLong("id"));
                            try (ResultSet setRows = setStatement.executeQuery()) {
                                while (setRows.next()) {
                                    exercise.addSet(setRows.getInt("metric1"), setRows.getDouble("metric2"));
                                }
                            }
                        }
                    }
                    result.add(bodyPart);
                }
            }
        }
        return result;
    }

    private ArrayList<WorkoutSession> loadSessions(Connection connection, long userId) throws SQLException {
        ArrayList<WorkoutSession> result = new ArrayList<>();
        Map<String, String> bodyPartByExerciseName = loadBodyPartNamesByExerciseName(connection, userId);
        String sessionSql = "SELECT id, session_date, session_name FROM workout_sessions WHERE user_id = ? ORDER BY id ASC";
        String exerciseSql = "SELECT id, name, type, body_part_name FROM session_exercises WHERE session_id = ? ORDER BY id ASC";
        String setSql = "SELECT metric1, metric2 FROM session_sets WHERE session_exercise_id = ? ORDER BY id ASC";

        try (PreparedStatement sessionStatement = connection.prepareStatement(sessionSql);
             PreparedStatement exerciseStatement = connection.prepareStatement(exerciseSql);
             PreparedStatement setStatement = connection.prepareStatement(setSql)) {
            sessionStatement.setLong(1, userId);
            try (ResultSet sessionRows = sessionStatement.executeQuery()) {
                while (sessionRows.next()) {
                    WorkoutSession session = new WorkoutSession(
                        sessionRows.getString("session_date"),
                        sessionRows.getString("session_name")
                    );
                    exerciseStatement.setLong(1, sessionRows.getLong("id"));
                    try (ResultSet exerciseRows = exerciseStatement.executeQuery()) {
                        while (exerciseRows.next()) {
                            String exerciseName = exerciseRows.getString("name");
                            String bodyPartName = exerciseRows.getString("body_part_name");
                            if (bodyPartName == null || bodyPartName.isBlank()) {
                                bodyPartName = bodyPartByExerciseName.get(exerciseName.toLowerCase());
                            }
                            if (bodyPartName == null || bodyPartName.isBlank()) {
                                bodyPartName = "History";
                            }
                            BodyPart historyBodyPart = new BodyPart(bodyPartName);
                            Exercise exercise = historyBodyPart.createExercise(
                                exerciseName,
                                ExerciseType.fromDisplayName(exerciseRows.getString("type"))
                            );
                            setStatement.setLong(1, exerciseRows.getLong("id"));
                            try (ResultSet setRows = setStatement.executeQuery()) {
                                while (setRows.next()) {
                                    exercise.addSet(setRows.getInt("metric1"), setRows.getDouble("metric2"));
                                }
                            }
                            session.addExercise(exercise);
                        }
                    }
                    result.add(session);
                }
            }
        }
        return result;
    }

    private Map<String, String> loadBodyPartNamesByExerciseName(Connection connection, long userId) throws SQLException {
        Map<String, String> result = new HashMap<>();
        String sql = """
            SELECT e.name AS exercise_name, b.name AS body_part_name
            FROM exercises e
            JOIN body_parts b ON b.id = e.body_part_id
            WHERE b.user_id = ?
            ORDER BY b.id ASC, e.id ASC
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.putIfAbsent(
                        rows.getString("exercise_name").toLowerCase(),
                        rows.getString("body_part_name")
                    );
                }
            }
        }
        return result;
    }

    private ArrayList<Reminder> loadReminders(Connection connection, long userId) throws SQLException {
        ArrayList<Reminder> result = new ArrayList<>();
        String sql = "SELECT label, scheduled_time, repeat_interval_days FROM reminders WHERE user_id = ? ORDER BY scheduled_time ASC, id ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int intervalDays = resultSet.getInt("repeat_interval_days");
                    if (resultSet.wasNull() || intervalDays <= 0) {
                        continue;
                    }
                    result.add(new Reminder(
                        resultSet.getString("label"),
                        LocalDateTime.parse(resultSet.getString("scheduled_time")),
                        intervalDays
                    ));
                }
            }
        }
        return result;
    }

    private Connection openConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("SQLite JDBC driver is not available.", e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    private long findUserId(Connection connection, String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("No user found for username " + username);
                }
                return resultSet.getLong("id");
            }
        }
    }

    private long insertUser(Connection connection, String username, String password, double weight, double height) throws SQLException {
        String sql = "INSERT INTO users(username, password, weight, height) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setDouble(3, weight);
            statement.setDouble(4, height);
            statement.executeUpdate();
            return generatedId(statement, "user");
        }
    }

    private void insertWeightHistory(Connection connection, long userId, List<Double> history) throws SQLException {
        String sql = "INSERT INTO weight_history(user_id, record_order, weight, record_date) VALUES (?, ?, ?, ?)";
        LocalDate startDate = LocalDate.now().minusDays(Math.max(0, history.size() - 1));
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < history.size(); i++) {
                statement.setLong(1, userId);
                statement.setInt(2, i);
                statement.setDouble(3, history.get(i));
                statement.setString(4, startDate.plusDays(i).toString());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private long insertBodyPart(PreparedStatement statement, long userId, String name) throws SQLException {
        statement.setLong(1, userId);
        statement.setString(2, name);
        statement.executeUpdate();
        return generatedId(statement, "body part");
    }

    private long insertExercise(PreparedStatement statement, long bodyPartId, Exercise exercise) throws SQLException {
        statement.setLong(1, bodyPartId);
        statement.setString(2, exercise.getName());
        statement.setString(3, exercise.getExerciseType().name());
        statement.executeUpdate();
        return generatedId(statement, "exercise");
    }

    private long insertSession(PreparedStatement statement, long userId, WorkoutSession session) throws SQLException {
        statement.setLong(1, userId);
        statement.setString(2, session.getDate());
        statement.setString(3, session.getSessionName());
        statement.executeUpdate();
        return generatedId(statement, "session");
    }

    private long insertSessionExercise(PreparedStatement statement, long sessionId, Exercise exercise) throws SQLException {
        statement.setLong(1, sessionId);
        statement.setString(2, exercise.getName());
        statement.setString(3, exercise.getExerciseType().name());
        String bodyPartName = exercise.getBodyPart().getName();
        if (bodyPartName == null || bodyPartName.isBlank() || "History".equalsIgnoreCase(bodyPartName)) {
            statement.setNull(4, java.sql.Types.VARCHAR);
        } else {
            statement.setString(4, bodyPartName);
        }
        statement.executeUpdate();
        return generatedId(statement, "session exercise");
    }

    private void insertExerciseSets(PreparedStatement statement, long exerciseId, Exercise exercise) throws SQLException {
        for (var setRecord : exercise.getSets()) {
            statement.setLong(1, exerciseId);
            statement.setInt(2, setRecord.getPrimaryMetricValue().intValue());
            statement.setDouble(3, setRecord.getSecondaryMetricValue().doubleValue());
            statement.addBatch();
        }
        statement.executeBatch();
        statement.clearBatch();
    }

    private void deleteWorkoutDraft(Connection connection, long userId) throws SQLException {
        String sql = """
            DELETE FROM exercise_sets
            WHERE exercise_id IN (
                SELECT e.id
                FROM exercises e
                JOIN body_parts b ON b.id = e.body_part_id
                WHERE b.user_id = ?
            )
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }

        try (PreparedStatement statement = connection.prepareStatement("""
            DELETE FROM exercises
            WHERE body_part_id IN (
                SELECT id FROM body_parts WHERE user_id = ?
            )
            """)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }

        deleteByUserId(connection, "body_parts", userId);
    }

    private void deleteSessions(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            DELETE FROM session_sets
            WHERE session_exercise_id IN (
                SELECT se.id
                FROM session_exercises se
                JOIN workout_sessions ws ON ws.id = se.session_id
                WHERE ws.user_id = ?
            )
            """)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }

        try (PreparedStatement statement = connection.prepareStatement("""
            DELETE FROM session_exercises
            WHERE session_id IN (
                SELECT id FROM workout_sessions WHERE user_id = ?
            )
            """)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }

        deleteByUserId(connection, "workout_sessions", userId);
    }

    private void deleteByUserId(Connection connection, String tableName, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE user_id = ?")) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    private long generatedId(PreparedStatement statement, String entity) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new IllegalStateException("No generated id returned for " + entity);
            }
            return keys.getLong(1);
        }
    }

    private boolean isUniqueConstraintViolation(SQLException exception) {
        String message = exception.getMessage();
        return message != null && message.toLowerCase().contains("unique");
    }

    public record UserCredentials(String username, String password) {
    }

    public record LoadedUserData(
        User user,
        ArrayList<BodyPart> bodyParts,
        ArrayList<WorkoutSession> sessions,
        ArrayList<Reminder> reminders
    ) {
    }
}
