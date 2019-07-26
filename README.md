<h1 align="center">
  <img src="https://xwr.visualstudio.com/XWare/_apis/build/status/Dockerhub%20SeicentoBilling-FromTemplate" alt="BuildStatus"/>
  <a href="http://hits.dwyl.io/xware-gmbh/SeicentoBilling">
  	<img src="http://hits.dwyl.io/xware-gmbh/SeicentoBilling.svg" alt="HitCount"/>
  </a>
  <img src="https://img.shields.io/github/stars/xware-gmbh/SeicentoBilling.svg?label=Stars&style=flat" alt="Stars"/>
  <a href="https://github.com/xware-gmbh/SeicentoBilling/blob/master/LICENSE.txt">
  	<img src="https://img.shields.io/github/license/xware-gmbh/SeicentoBilling.svg" alt="License"/>
  </a>
</h1>
<h1 align="left">
  <img src="https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/seicento_billing.PNG" alt="ProductLogo"/>
   <img src="https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/XWareLogo.png" alt="Company Logo" align="right"/>
</h1>
         
SeicentoBilling ist eine einfache Weblösung für Leistungsrapportierung, Spesen und Rechnungsstellung. Die App ist seit mehreren Jahren produktiv im Einsatz.  

Die Sourcen der Lösung sind auf [Github](https://github.com/xware-gmbh/SeicentoBilling) unter der Apache 2.0 Lizenz verfügbar.
Ein Docker Image kann von [Dockerhub](https://cloud.docker.com/repository/docker/jmurihub/seicentobilling/general) bezogen werden.

## Toolstack
Folgende technische Komponenten kommen zum Einsatz
* [Rapidclipse IDE (Eclipse basiert)](http://rapidclipse.com) (Java, Vaadin, Hibernate ....)
* [Tomcat 9.0](https://tomcat.apache.org/download-80.cgi)
* MSSQL als DB
* [Jasperserver](https://community.jaspersoft.com/project/jasperreports-server)
* [Docker](https://docker.com)
* Azure AD
 

## Installation SeicentoBilling als Docker Image
Folgende Voraussetzungen müssen erfüllt sein für die Installation:
* bestehendes Azure AD Konto
* Docker (auf lokaler Maschine mit Docker Desktop unter Windows - ab Version 18.x)
* MSSQL DB (ab Version 12)

### Installation Steps
1. Bereitstellen einer SQL DB ([Anleitung](https://github.com/xware-gmbh/SeicentoBilling/tree/master/flyway)) 
2. Registrieren einer App im Azure Portal ([Anleitung](https://github.com/xware-gmbh/SeicentoBilling/tree/master/docs/azuread))
3. Starten des Docker Containers ([Anleitung](https://github.com/xware-gmbh/SeicentoBilling/tree/master/docs/docker))

Optional:
* Setup Jasperserver/Reports ([Anleitung](https://github.com/xware-gmbh/SeicentoBilling/tree/master/docs/jasperserver))
* Erstellen eines Reverse Proxy mit nginx ([Anleitung](https://github.com/xware-gmbh/SeicentoBilling/tree/master/docs/nginx))

## Features
Folgende Module/Features werden durch SeicentoBilling behandelt:
![Features](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/billing_modules.PNG "Logo")   
![StartScreen](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/SeicentoBilling_Overview.PNG "StartScreen")

