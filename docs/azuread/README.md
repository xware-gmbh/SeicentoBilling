# SeicentoBilling - Azure AD
Azure Active Directory (AAD) stellt eine Authentifizierungsmöglichkeit für Cloudlösungen bereit. SeicentoBilling nutzt diese entsprechend. Wir werden im AD SeicentoBilling als App eröffnen und die Callback URL auf localhost konfigurieren.  

Voraussetzung ist, dass eine Subscription und die nötigen Berechtigungen in Azure vorhanden sind.
       


### Installation Steps
1. Starten [Azure Portal](https://portal.azure.com) 
3. Wählen der [App Registrierung](https://portal.azure.com/#blade/Microsoft_AAD_IAM/ActiveDirectoryMenuBlade/RegisteredAppsPreview)
4. Callback Adresse erfassen (Server auf welchem Docker laufen wird)
5. SecretKey generieren (wird für die Konfiguration benötigt)

Step 2: __+ Neue Registrierung__. Nennen wir die Anwendung _SeicentoBilling_    
![Azureapp Registration](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/azure_appreg_step1.PNG "Azureapp Registration")   
![Azureapp Registration](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/azure_appreg_step2.PNG "Azureapp Registration")

Step 3: erfassen Callback URI. Typ: Web Uri: __http://localhost:8080__   
![Azureapp URL](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/azure_appreg_step3.PNG "Azureapp URL")

Step 4: generieren _Secret_. Der Wert muss bei der Erstellung kopiert werden, da dieser nachher nicht mehr ersichtlich ist. Dieser wird für die Konfiguration von docker-compose benötigt.   
![Azureapp secret](https://github.com/xware-gmbh/SeicentoBilling/blob/master/docs/images/azure_appreg_step4.PNG "Azureapp secret")

## Next Step
Docker ([Anleitung](https://github.com/xware-gmbh/SeicentoBilling/tree/master/docs/docker))   
 
