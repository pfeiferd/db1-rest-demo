# DB1: REST-Demo

Begleitprojekt zum Kapitel **„Von der Datenbank zum Informationssystem“** der Vorlesung Datenbanken 1.
Es enthält alle Beispiele der Folien zum Ausprobieren:

- **REST mit Jakarta REST (JAX-RS)** am Beispiel der Artikelarten aus der Lagerverwaltung – ohne Spring,
  mit der Implementierung Jersey im Servlet-Container Jetty
- die **Mini-Studie zum `PersonService`** im Stil von [gweb](https://github.com/pfeiferd/gweb):
  Dienstschnittstelle, DTOs, JDBC, Rechteprüfung, Write-Through-Cache und HTTP-Schnittstelle als
  Delegationskette – mit einer H2-Datenbank im Hauptspeicher
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

Artikelarten (ressourcenorientiert):

| Request | Ergebnis |
|---|---|
| [GET /api/artikelarten](http://localhost:8080/api/artikelarten) | alle Artikelarten |
| [GET /api/artikelarten?maxGewicht=1](http://localhost:8080/api/artikelarten?maxGewicht=1) | gefiltert |
| [GET /api/artikelarten/A1](http://localhost:8080/api/artikelarten/A1) | eine Artikelart |
| [GET /api/artikelarten/A9](http://localhost:8080/api/artikelarten/A9) | `404 Not Found` |

PersonService (im Stil von gweb):

| Request | Ergebnis |
|---|---|
| [GET /api/PersonService/getAll](http://localhost:8080/api/PersonService/getAll) | alle Personen |
| [GET /api/PersonService/get/1](http://localhost:8080/api/PersonService/get/1) | eine Person |
| [GET /api/PersonService/get/99](http://localhost:8080/api/PersonService/get/99) | `404 Not Found` |

Mit `curl` im Terminal:

```bash
curl http://localhost:8080/api/artikelarten/A1

curl -i -X POST -H 'Content-Type: application/json' \
  -d '{"aid":"A4","aname":"Kaffee","gewicht":0.5}' \
  http://localhost:8080/api/artikelarten

curl -i -X PUT -H 'Content-Type: application/json' \
  -d '{"aname":"Kaffee, gemahlen","gewicht":0.5}' \
  http://localhost:8080/api/artikelarten/A4

curl -i -X DELETE http://localhost:8080/api/artikelarten/A4
```

## Weitere Programme

Bei laufendem Server ruft der **Java-Client** die Artikelarten über das Client-API von Jakarta REST ab:

```bash
mvn exec:java -Dexec.mainClass=demo.lager.ArtikelartClient
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
3. Die Klassen `ArtikelartClient`, `tcp.Server` und `tcp.Client` lassen sich direkt über ihre
   `main`-Methode starten (grüner Pfeil am Rand)

Änderungen am Java-Code übernimmt `jetty:run` nach dem Neukompilieren (*Build → Build Project*)
nach wenigen Sekunden von selbst.

## Aufbau

```
src/main/java/demo/
  DemoApplication.java         Jakarta-REST-Anwendung: registriert Ressourcen und baut die Dienstkette
  DemoDatabase.java            H2-Datenbank im Hauptspeicher mit Tabelle person
  lager/                       Artikelarten: Artikelart, ArtikelartResource, ArtikelartClient
  service/                     Dienstschnittstellen: CRUDService, PersonService, UserService
    dto/                       DTO-Klassen: DTO, Person, User
    jdbc/                      Datenzugriff: PersonJDBCService
    role/                      Rechteprüfung: PersonRoleService, UserStore, DemoUserStore
    cache/                     Write-Through-Cache: PersonCacheService
    rest/                      HTTP-Schnittstelle: PersonRestService, ServiceExceptionMapper
  tcp/                         TCP-Beispiel: Server und Client
src/main/webapp/
  index.html                   Startseite mit den Demo-Links
  WEB-INF/web.xml              bindet Jersey unter /api/* ein
```

Die Delegationskette des `PersonService` (in `DemoApplication`):

```
PersonRestService → PersonRoleService → PersonCacheService → PersonJDBCService → H2
```

**Vereinfachungen für die Demo:** Angemeldet ist immer dieselbe Admin-Person (`DemoUserStore`) – eine echte
Anwendung holt den User nach einem Login aus der HTTP-Session. Die Artikelarten liegen nur im Hauptspeicher,
die Personen in einer H2-Datenbank, die bei jedem Start neu angelegt wird.
