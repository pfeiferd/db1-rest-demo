# DB1: REST-Demo

Begleitprojekt zum Kapitel **„Von der Datenbank zum Informationssystem“** der Vorlesung Datenbanken 1.
Es enthält alle Beispiele der Folien zum Ausprobieren:

- **REST mit JAX-RS (Jakarta REST)** am Beispiel einer Ressource für Personen – ohne Spring,
  in einem Servlet-Container (hier Jetty) mit einer JAX-RS-Implementierung (hier Jersey)
- die **Mini-Studie zum `PersonService`** im Stil von [gweb](https://github.com/pfeiferd/gweb):
  Dienstschnittstelle, DTOs, JDBC, Rechteprüfung, Write-Through-Cache und HTTP-Schnittstelle als
  Delegationskette – mit einem eingebetteten SQL-DBMS (hier H2), das die Daten im Hauptspeicher hält
- das **TCP-Beispiel** mit einem Server, der Zeichenketten umdreht

## Voraussetzungen

- **Java 17** oder neuer (`java -version`)
- **Maven 3.8** oder neuer (`mvn -version`) – in IntelliJ bereits enthalten

Alles Weitere (Jetty, Jersey, H2, …) lädt Maven beim ersten Start automatisch herunter.

## Starten

Im Projektordner:

```bash
mvn jetty:run
```

Sobald `Started … ROOT` erscheint, läuft der Server. Dann im Browser öffnen:

**<http://localhost:8080/>**

Die Startseite enthält Links für alle `GET`-Requests und Knöpfe für `POST`, `PUT` und `DELETE`;
Request und Response erscheinen unten auf der Seite. Beenden mit `Strg+C`.

Läuft schon etwas auf Port 8080, startet man den Server auf einem anderen Port:

```bash
mvn jetty:run -Djetty.port=8081
```

## Direkt ausprobieren

Personen (ressourcenorientiert):

| Request | Ergebnis |
|---|---|
| [GET /api/personen](http://localhost:8080/api/personen) | alle Personen |
| [GET /api/personen?lastName=Turing](http://localhost:8080/api/personen?lastName=Turing) | gefiltert |
| [GET /api/personen/1](http://localhost:8080/api/personen/1) | eine Person |
| [GET /api/personen/99](http://localhost:8080/api/personen/99) | `404 Not Found` |

PersonService (im Stil von gweb):

| Request | Ergebnis |
|---|---|
| [GET /api/PersonService/getAll](http://localhost:8080/api/PersonService/getAll) | alle Personen |
| [GET /api/PersonService/get/1](http://localhost:8080/api/PersonService/get/1) | eine Person |
| [GET /api/PersonService/get/99](http://localhost:8080/api/PersonService/get/99) | `404 Not Found` |

Mit `curl` im Terminal:

```bash
curl http://localhost:8080/api/personen/1

curl -i -X POST -H 'Content-Type: application/json' \
  -d '{"firstName":"Grace","lastName":"Hopper"}' \
  http://localhost:8080/api/personen

curl -i -X PUT -H 'Content-Type: application/json' \
  -d '{"firstName":"Grace","lastName":"Murray Hopper"}' \
  http://localhost:8080/api/personen/3

curl -i -X DELETE http://localhost:8080/api/personen/3
```

## Weitere Programme

Bei laufendem Server ruft der **Java-Client** die Personen über die Client-API von JAX-RS ab:

```bash
mvn exec:java -Dexec.mainClass=demo.personen.PersonClient
```

Das **TCP-Beispiel** braucht zwei Terminals:

```bash
mvn exec:java -Dexec.mainClass=demo.tcp.Server                    # Terminal 1
mvn exec:java -Dexec.mainClass=demo.tcp.Client -Dexec.args=Datenbank   # Terminal 2: knabnetaD
```

## In IntelliJ

1. *File → Open…* und den Projektordner (mit der `pom.xml`) wählen – IntelliJ erkennt das Maven-Projekt
2. Im Maven-Fenster (rechts) unter *Plugins → jetty* doppelt auf **`jetty:run`** klicken – oder im
   Terminal von IntelliJ `mvn jetty:run` eingeben
3. Die Klassen `PersonClient`, `tcp.Server` und `tcp.Client` lassen sich direkt über ihre
   `main`-Methode starten (grüner Pfeil am Rand)

Änderungen am Java-Code übernimmt `jetty:run` nach dem Neukompilieren (*Build → Build Project*)
nach wenigen Sekunden von selbst.

## Aufbau

```
src/main/java/demo/
  DemoApplication.java         JAX-RS-Anwendung: registriert Ressourcen und baut die Dienstkette
  DemoDatabase.java            eingebettetes SQL-DBMS (H2) im Hauptspeicher mit Tabelle person
  personen/                    REST-Ressource: PersonResource, PersonClient
  service/                     Dienstschnittstellen: CRUDService, PersonService, UserService
    dto/                       DTO-Klassen: DTO, Person, User
    jdbc/                      Datenzugriff: PersonJDBCService
    role/                      Rechteprüfung: PersonRoleService, UserStore, DemoUserStore
    cache/                     Write-Through-Cache: PersonCacheService
    rest/                      HTTP-Schnittstelle: PersonRestService, ServiceExceptionMapper
  tcp/                         TCP-Beispiel: Server und Client
src/main/webapp/
  index.html                   Startseite mit den Demo-Links
  WEB-INF/web.xml              Deployment-Deskriptor: bindet das JAX-RS-Servlet unter /api/* ein
```

Die Delegationskette des `PersonService` (in `DemoApplication`):

```
PersonRestService → PersonRoleService → PersonCacheService → PersonJDBCService → SQL-DBMS
```

**Vereinfachungen für die Demo:** Angemeldet ist immer dieselbe Admin-Person (`DemoUserStore`) – eine echte
Anwendung holt den User nach einem Login aus der HTTP-Session. Die `PersonResource` hält ihre Personen nur im Hauptspeicher,
der `PersonService` in einem eingebetteten SQL-DBMS (H2), dessen Datenbank bei jedem Start neu angelegt wird.
