# SeicentoBilling - Jasper Reports  
[Jasper Reports](https://community.jaspersoft.com/project/jasperreports-server) ist ein Reporting Tool Design + Server, welches zusammen mit SeicentoBilling genutzt werden kann.

### Setup
1. Startup Docker Image: _docker run -itd -p 8080:8080 jmurihub/jasperserver:6.3.0_
2. Load Reports into Server
3. Anpassen DB Connection + Credentials
4. Testreport starten.
5. Konfigurieren Seicento

#### Step 1: Start Docker Image
Nachdem der Container mit    _docker run -itd -p 8080:8080 jmurihub/jasperserver:6.3.0_    
von der Kommandozeile erfolgreich gestartet wurde, ist Jasper unter der URL _localhost:8080/Jasperserver_ erreichbar.   
Anmelden mit jasperadmin/jasperadmin

#### Step 2: Load Reports into Server
1. Herunterladen der Datei JasperExport_LocalDocker.zip (in diesem Verzeichnis)
2. Laden dieser Datei in Jasper unter: Verwalten/Servereinstellungen/Importieren
 
#### Step 3: Anpassen DB Connection + Credentials
1. Unter Root/Data Sources __MSSQL 2012 Express (Aktiv)__ überprüfen (Bearbeiten/Test Verbindung)
2. Unter Verwalten/Benutzer - ändern Passwort für Benutzer jasperadmin + jasperuser. Das Passwort für jasperuser wird später bei der Konfiguration von Seicento benötigt.

#### Step 4: Testreport starten
Beliebigen Report unter Root/Reports/XWare GmbH aufstarten.

![Jasperserver](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/Jasper_BillingOverview.PNG "Jasperserver")

#### Step 5: Seicento Konfigurieren
In Seicento unter Optionen/Firma auf dem Tab Jasper die Credentials für den Benutzer jasperuser eintragen.
Unter der URL sollte folgender Werte stehen:
 
    http://localhost:8080/jasperserver/flow.html?_flowId=viewReportFlow&standAlone=true&_flowId=viewReportFlow&ParentFolderUri=%2Freports%2FXWare_GmbH&reportUnit=%2Freports%2FXWare_GmbH%2F{0}
                                                                  
   
### Links
* [Docker Image](https://cloud.docker.com/u/jmurihub/repository/docker/jmurihub/jasperserver)
   