# FitnessApp Frontend
Die Anwendung ermöglicht Benutzern das Verwalten von Fitnessübungen, das Erstellen von Trainingsplänen und das Planen sowie Protokollieren von Trainingssessions.

## 🚀 Features

### Übungsverwaltung (Exercises)
- **Übersicht:** Auflistung aller verfügbaren Übungen mit Name, Kategorie und Muskelgruppen.
- **Erstellen:** Hinzufügen neuer Übungen mit Dropdown-Auswahl für Kategorien (Gerät, Freihantel, Körpergewicht).
- **Details & Bearbeiten:** Ansehen und Editieren von Übungsdetails.
- **Löschen:** Entfernen von Übungen aus der Datenbank.

### Trainingspläne (Training Plans)
- **Übersicht:** Liste aller Trainingspläne inkl. der Anzahl enthaltener Sessions.
- **Verwaltung:** Erstellen neuer Pläne und Bearbeiten von Metadaten (Name, Beschreibung).
- **Session-Management:**
  - Hinzufügen von Trainingseinheiten zu einem Plan.
  - Zuweisung von Übungen zu einer Session (Mehrfachauswahl möglich).
  - Löschen von Sessions.
  - **Status-Tracking:** Umschalten des Status einer Session zwischen "GEPLANT" und "ABGESCHLOSSEN".

## 🛠️ Technologien

- **Framework:** Angular 21.0.1 (Standalone Components)
- **Sprache:** TypeScript
- **Styling:** Bootstrap 5 (Responsive Design)
- **Kommunikation:** HTTP Client (REST API)
- **Build Tool:** Angular CLI

## 📋 Voraussetzungen

Damit das Frontend funktioniert, müssen folgende Voraussetzungen erfüllt sein:

1. Node.js: (LTS Version empfohlen) muss installiert sein.
2. Backend: Das zugehörige Spring Boot Backend muss lokal laufen (Standard-Port: `8080`).
3. Das Backend muss CORS für `http://localhost:4200` aktiviert haben.

Installation & Start

1. In das Verzeichnis wechseln:
    cd fitness-frontend
    
2. Abhängigkeiten installieren:
    Lädt alle benötigten Pakete (wie Angular Core, Bootstrap) herunter.
    npm install
3. Anwendung starten:
    Startet den lokalen Entwicklungsserver.
    npm start
4. Im Browser öffnen:
    Die Anwendung ist nun unter `http://localhost:4200/` erreichbar.

Projektstruktur

Die wichtigsten Ordner und Dateien im Überblick:

- src/app/components: Enthält die UI-Komponenten (Listen, Detailansichten).
- exercise-list & exercise-detail
- training-plan-list & training-plan-detail
- src/app/services: Enthält den FitnessService für die API-Kommunikation mit dem Backend.
- src/app/models: TypeScript-Interfaces für Datentypen (Exercise, TrainingPlan, etc.).
- app.routes.ts: Definition der Routen (Navigation)
