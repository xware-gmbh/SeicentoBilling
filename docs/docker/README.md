# Docker Desktop
Wir gehen davon aus, dass Docker Desktop auf der Windows Maschine installiert ist und Grundkenntnisse für Docker vorhanden sind.
Anleitung für die Installation findet man hier: [Docker Desktop](https://docs.docker.com/docker-for-windows/install/)    
![Logo](https://upload.wikimedia.org/wikipedia/commons/archive/7/79/20140516082115%21Docker_%28container_engine%29_logo.png)

## Instruction Steps
1. Erstellen Arbeitsverzeichnis z.b _C:\xtra\docker\SeicentoBilling_
2. Kopieren [docker-compose-simple.yml](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docker/docker-compose-simple.yml) nach _C:\xtra\docker\SeicentoBilling\docker-compose.yml_
3. Anpassen docker-compose.yml
4. Starten Container

### docker-compose.yml
Anpassen docker-compose.yml. Die folgenden Werte unter _environment_ müssen gesezt werden:  

---
APP_STAGE=TEST  
DB_URL TEST=jdbc:sqlserver://[SERVER]:1433;database=[DBNAME];encrypt=true;trustServerCertificate=false; hostNameInCertificate=*.database.windows.net;loginTimeout=30;  
DB_USR TEST=[DBUSER]  
DB_PWD TEST=[DBPASSWORD]  
tenantid=[Company.com]  
clientid=[Azure ClientID]  
clientkey=[Azure Client Key]
SEICENTO_LOGIN_METHOD=local
  

---

__APP_STAGE__ kann einen beliebigen Wert haben. Dieser Wert wird als Suffix verwendet für die weiteren Keys.  
__DB_URL_TEST__ Die Werte für _[SERVER]_ und _[DBNAME]_ sind zu ersetzen (z.b. localhost + seicento)  
__DB_USR_TEST__ Der Wert für _[DBUSER]_ ist zu ersetzen (z.b. usrseicento)   
__DB_PWD_TEST__ Der Wert _[DBPASSWORD]_ ist mit dem effektiven Passwort zu setzen (Klartext).  
__SEICENTO_LOGIN_METHOD__ Definiert wie man sich authentifiziert. Mögliche Werte: local (User in XML), azure (Microsoft aad)  

Nur relevant bei Login Methode azure:  
__tenantid__ Der Wert für _[Company.com]_ ist mit dem Domänen Namen zu ersten aus der Azure Subscription (z.B. xwr.ch).  
__cientid__  Der Wert für _[Azure ClientID]_ ist mit der 36 stelligen Anwendungs-Id aus der App Registrierung zu ersetzen.  
__clientkey__ Der Wert für _[Azure Client Key]_ ist mit dem _Secret_ aus der App Registrierung zu ersetzen.   

### Starten container
Auf der Kommandozeile nach _C:\xtra\docker\SeicentoBilling_ navigieren.  
Container starten mit _docker-compose up -d_

Wenn alles korrekt ist kann nun das GUI via Browser aufgerufen werden [http://localhost:8080/SeicentoBilling](http://localhost:8080/SeicentoBilling)

  
 