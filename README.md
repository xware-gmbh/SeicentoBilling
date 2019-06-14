# SeicentoSalary ![xwr.ch](https://github.com/jmurighub/SeicentoSalary/blob/master/docs/images/XWareLogo.png "xwr.ch")
![Produktlogo](https://github.com/jmurighub/SeicentoSalary/blob/master/docs/images/seicento_salary.png "Logo") [![Build status](https://xwr.visualstudio.com/XWare/_apis/build/status/Dockerhub%20SeicentoSalary-FromTemplate-Task)](https://xwr.visualstudio.com/XWare/_build/latest?definitionId=22)         
SeicentoSalary ist eine Weblösung für die Verwaltung von Lohnzahlungen nach Schweizer Recht. Die App ist seit mehreren Jahren produktiv im Einsatz.  

Die Sourcen der Lösung sind auf [Github](https://github.com/jmurighub/SeicentoSalary) unter der Apache 2.0 Lizenz verfügbar.
Ein Docker Image kann von [Dockerhub](https://cloud.docker.com/repository/docker/jmurihub/seicentosalary/general) bezogen werden.

## Toolstack
Folgende technische Komponenten kommen zum Einsatz
* [Rapidclipse IDE (Eclipse basiert)](http://rapidclipse.com) (Java, Vaadin, Hibernate ....)
* [Tomcat 8.5](https://tomcat.apache.org/download-80.cgi)
* MSSQL als DB
* [Jasperserver](https://community.jaspersoft.com/project/jasperreports-server)
* [Docker](https://docker.com)
* Azure AD
 

## Installation SeicentoSalary als Docker Image
Folgende Voraussetzungen müssen erfüllt sein für die Installation:
* bestehendes Azure AD Konto
* Docker
* MSSQL DB

### Installation Steps
1. Bereitstellen einer SQL DB ([Anleitung](https://github.com/jmurighub/SeicentoSalary/tree/master/flyway)) 
3. Registrieren einer App im Azure Portal ([Anleitung](https://github.com/jmurighub/SeicentoSalary/tree/master/docs/azuread))
4. Erstellen von [docker-compose.yml](https://github.com/jmurighub/SeicentoSalary/blob/master/docker/docker-compose.yml) auf der Docker Maschine
    - Erstellen eines Reverse Proxy mit nginx ([Anleitung](https://github.com/jmurighub/SeicentoSalary/tree/master/docs/nginx))
5. Setzen der ENV Variablen in docker-compose mit den Werten aus Schritt 1. und 3.
6. Starten Image (_docker-compose up -d_)

Optional:
* Setup Jasperserver/Reports ([Anleitung](https://github.com/jmurighub/SeicentoSalary/tree/master/docs/jasperserver))
