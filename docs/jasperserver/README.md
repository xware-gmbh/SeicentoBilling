# SeicentoBilling - Jasper Reports  
[Jasper Reports](https://community.jaspersoft.com/project/jasperreports-server) ist ein Reporting Tool Design + Server, welches zusammen mit SeicentoBilling genutzt werden kann.

### Setup
1. Beziehen (Clone) Scripts von [Github - js-docker](https://github.com/xware-gmbh/js-docker)
2. Anpassen .env Datei mit DB Angaben (DB-TYPE, DB-HOST, DB-NAME, User, Pw)
3. Starten images "docker-compose up" (DB wird erstellt, wenn nicht vorhanden und JasperServer wird gestartet)
4. Login Jasperserver (jasperadmin)
5. Import/Load Reports (Verwalten/Servereinstellung/Importieren)
6. Setzen Passwort für jasperadmin
7. Konfigurieren DB Verbindung für Reports (Repository/Data Sources/SeicentoBilling (aktiv))  
8. Testreport starten
9. Konfigration Seicento überprüfen


#### Step 5: Load Reports into Server
1. Herunterladen der Datei JasperExport_LocalDocker.zip (in diesem Verzeichnis)
2. Laden dieser Datei in Jasper unter: Verwalten/Servereinstellungen/Importieren
 
#### Step 7: Anpassen DB Connection + Credentials
1. Unter Root/Data Sources __SeicentoBilling (aktiv)__ überprüfen (Bearbeiten/Test Verbindung)
2. Unter Verwalten/Benutzer - ändern Passwort für Benutzer jasperadmin + jasperuser. Das Passwort für jasperuser wird später bei der Konfiguration von Seicento benötigt.

#### Step 8: Testreport starten
Beliebigen Report unter Root/Reports/XWare GmbH aufstarten.

![Jasperserver](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/Jasper_BillingOverview.PNG "Jasperserver")

#### Step 9: Seicento Konfigurieren
In Seicento unter Optionen/Firma auf dem Tab Jasper die Credentials für den Benutzer jasperuser eintragen.
Unter der URL sollte folgender Werte stehen:
 
    http://localhost:8080/jasperserver/flow.html?_flowId=viewReportFlow&standAlone=true&_flowId=viewReportFlow&ParentFolderUri=%2Freports%2FXWare_GmbH&reportUnit=%2Freports%2FXWare_GmbH%2F{0}
                                                                  
   
### Links
* [Docker Image](https://cloud.docker.com/u/jmurihub/repository/docker/jmurihub/jasperserver)
   