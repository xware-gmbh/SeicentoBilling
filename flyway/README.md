# Bereitstellen einer SQL DB
Zuerst soll eine DB zur Verfügung gestellt werden. Wir verwenden MS SQL in der Version 12. Andere relationalen DB's sind möglich.


## Instruction Steps
1. Neue Datenbank erstellen mit [SQL Managment Studio](https://docs.microsoft.com/en-us/sql/ssms/download-sql-server-management-studio-ssms?view=sql-server-2017)
2. Herunterladen von [Flyway for Command Line](https://flywaydb.org/documentation/commandline/#download-and-installation)
3. Konfigurieren  conf/flyway.conf mit den DB Credentials von Schritt 1
4. Kopieren der [SQL](https://github.com/xware-gmbh/SeicentoBilling/tree/master/flyway/sql) Scripts nach flyway (Directory flyway/sql)
5. ausführen _flyway info_ auf der Kommandozeile
6. ausführen _flyway migrate_ auf der Kommandozeile

### DB erstellen
Voraussetzung für das Setup der DB mittels flyway ist, dass eine neue DB vorbereitet ist. Konkret braucht es einen DB Namen und einen Benutzer mit der Berechtigung für das Erstellen von Tabellen.

Neue DB eröffnen im SQl Management Studio, z.B. mit Namen __seicento__:
![sql management studio](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/sqlman_newdb.PNG "sql managment studio")

Für die DB (seicento) einen User eröffnen/zuteilen. Nennen wir diesen "__usrseicento__". Im Studio unter Sicherheit/Anmeldungen "Neue Anmeldung..."  
![sql management studio](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/sqlman_newuser.PNG "sql managment studio")

Der DB User muss über die entsprechenden Berechtigungen verfügen um Tabellen anzulegen. Testweise kann man sich sich mit dem erstellten User im SQL Management Studio anmelden.
 

### DB mit flyway aufbauen
![Produktlogo](https://flywaydb.org/assets/logo/flyway-logo-tm-sm.png "Flyway Logo")  Mit [Flyway](https://flywaydb.org/) erstellen wir die Tabellen für die Applikation. Mit den Tabellen werden auch gleich benötigte Daten in dieselben geschrieben. Im folgenden nun wird beschrieben, wie flyway installert und genutzt wird.

Nachdem flyway heruntergeladen ist (Link unter Step 2) kann die ZIP Datei (im Beispiel _flyway-commandline-5.2.4-windows-x64.zip_) in ein beliebiges Zielverzeichnis entpackt werden (z.B. _C:\xtra\tryout\flyway-5.2.4_)

Nun muss flyway bekannt gegeben werden, welches unsere Ziel DB sein soll. Hierfür editieren wir die Datei _conf/flyway.conf_ und setzen folgende Key/Values:

---
__flyway.url__=jdbc:sqlserver://localhost:1433;database=seicento;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;  
__flyway.user__=usrseicento  
__flyway.password__=[your password]  

---

### Test flyway
Um flyway zu testen wie folgt vorgehen:  
* Starten CMD (Kommandozeile)
* Navigieren nach C:\xtra\tryout\flyway-5.2.4
* ausführen _flyway info_

Output flyway info könnte in etwa so aussehen:  
![flyway info](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/flyway-infosalary.PNG "flyway info")   

### DB migrieren mit flyway
Um die Tabellen zu erstellen sind nun noch folgende Schritte nötig:
* Sql Scripts kopieren (von Github _SeicentoBilling/flyway/sql_ nach _C:\xtra\tryout\flyway-5.2.4\sql_)   
    Kopierte SQL Dateien:    
    ![flywaysql](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/flyway-copyscripts.PNG "flyway sql"))
* ausführen _flyway migrate_ von der Kommandozeile

Nachdem _flyway migrate_ durchgelaufen ist, sollten nun die Tabellen vorhanden sein:  ![sql management studio](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/sqlmanagmentstudio.PNG "sql managment studio")  )

## Next Step
Registrieren einer App im Azure Portal ([Anleitung](https://github.com/xware-gmbh/SeicentoBilling/tree/master/docs/azuread))   
 