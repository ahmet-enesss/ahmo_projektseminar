Verwendete Technologien
Java 21
Spring Boot 3.5.7
Spring Web (für REST-APIs)
Spring Data JPA (für Datenbankzugriff)
Spring Boot Validation (zur Validierung von Eingabedaten)
H2 Database (flüchtige In-Memory-Datenbank)
Lombok (zur Reduzierung von Boilerplate-Code)
Maven (als Build-Tool)

Das Projekt basiert auf drei Haupt-Entitäten:
Exercise (Übung)
Stellt eine einzelne Fitnessübung dar (z.B. "Bankdrücken").
Felder: id, name (eindeutig), category, muscleGroups (Set von Strings), description.

TrainingPlan (Trainingsplan)
Definiert einen übergeordneten Trainingsplan (z.B. "Push Day").
Felder: id, name (eindeutig), description.
Hat eine 1:n-Beziehung zu TrainingSession.

TrainingSession (Trainingseinheit)
Stellt eine geplante Trainingseinheit an einem bestimmten Datum dar.
Felder: id, name, scheduledDate, status (GEPLANT, ABGESCHLOSSEN).
Hat eine n:1-Beziehung zu TrainingPlan1 (jede Einheit gehört zu einem Plan).
Hat eine n:m-Beziehung zu Exercise1 (jede Einheit kann mehrere Übungen enthalten).

Die Anwendung stellt die folgenden REST-Endpunkte unter dem Basispfad /api bereit:

Übungen (/api/exercises)
GET `/api/exercises`: Ruft eine Liste aller verfügbaren Übungen ab.
GET `/api/exercises/{id}`: Ruft eine bestimmte Übung anhand ihrer ID ab. 
POST `/api/exercises`: Erstellt eine neue Übung.
PUT `/api/exercises/{id}`: Aktualisiert eine bestehende Übung.
DELETE `/api/exercises/{id}`: Löscht eine Übung.

Trainingspläne (/api/trainingplans)
GET `/api/trainingplans`: Ruft eine Liste aller Trainingspläne ab.
GET `/api/trainingplans/{id}`: Ruft einen bestimmten Trainingsplan (inkl. Sessions) ab.
POST `/api/trainingplans`: Erstellt einen neuen Trainingsplan.
PUT `/api/trainingplans/{id}`: Aktualisiert Name/Beschreibung eines Plans.
DELETE `/api/trainingplans/{id}`: Löscht einen Trainingsplan.

Trainingseinheiten (/api/trainingsessions)
GET `/api/trainingsessions`: Ruft eine Liste aller Trainingseinheiten ab.
GET `/api/trainingsessions/{id}`: Ruft eine bestimmte Trainingseinheit ab.
POST `/api/trainingsessions`: Erstellt eine neue Trainingseinheit, die einem Plan zugeordnet ist.
PUT `/api/trainingsessions/{id}`: Aktualisiert eine bestehende Trainingseinheit (z.B. Status ändern).
DELETE `/api/trainingsessions/{id}`: Löscht eine Trainingseinheit.

Setup & Starten

Das Projekt ist ein Standard-Maven-Projekt und kann mit dem mitgelieferten Maven Wrapper gestartet werden.
Kompilieren & Starten:
Die Anwendung wird standardmäßig auf http://localhost:8080 ausgeführt.

Datenbank
Das Projekt verwendet eine flüchtige H2 In-Memory-Datenbank. Die Daten gehen bei jedem Neustart verloren.
Beim Start wird die Datenbank automatisch mit Beispieldaten (1 Übung: "Bankdrücken", 1 Plan: "Push Day") initialisiert, sofern sie leer ist (siehe FitnessAppApplication.java).
Die H2-Konsole ist aktiviert und unter http://localhost:8080/h2-console erreichbar.

JDBC URL: jdbc:h2:mem:fitnessdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
Benutzername: sa
Passwort: 