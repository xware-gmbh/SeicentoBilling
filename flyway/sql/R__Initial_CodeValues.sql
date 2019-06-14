/****** CostAccount ****/            
IF (select count(*) FROM [dbo].[CostAccount]) = 0  
BEGIN 
    INSERT INTO [dbo].[CostAccount]   
        ([csaCode]  
        ,[csaName]  
        ,[csaState])  
    VALUES 
        ('admin', 'local admin', 1)
 
END             


/****** City ****/            
IF (select count(*) FROM [dbo].[City]) = 0  
BEGIN 
    INSERT INTO [dbo].[City]   
        ([ctyName]  
        ,[ctyCountry]  
        ,[ctyRegion]  
        ,[ctyGeoCoordinates]  
        ,[ctyZIP]  
        ,[ctyState])  
    VALUES 
        ('Sursee', 'CH', 'LU', '47.1780497:8.0710555', '6210', 1),
        ('Luzern', 'CH', 'LU', '47.1780497:8.0710555', '6000', 1),
        ('Oberhausen', 'DE', '', '47.1780497:8.0710555', '46047', 1),
        ('Oberkirch', 'CH', 'LU', '47.1780497:8.0710555', '6208', 1)
 
END             

/****** PaymentCondition ****/            
IF (select count(*) FROM [dbo].[PaymentCondition]) = 0  
BEGIN 
    INSERT INTO [dbo].[PaymentCondition]   
        ([pacCode]  
        ,[pacName]  
        ,[pacNbrOfDays]  
        ,[pacState])  
    VALUES 
        ('30N', 'Zahlbar netto innert 30 Tagen', 30, 1),
        ('1N', 'Zahlbar netto bei Erhalt', 1, 1),
        ('10N', 'Zahlbar netto innert 10 Tagen', 10, 1),
        ('20N', 'Zahlbar netto innert 20 Tagen', 20, 1),
        ('60N', 'Zahlbar netto innert 60 Tagen', 60, 1)
 
END             

/****** Vat ****/            
IF (select count(*) FROM [dbo].[Vat]) = 0  
BEGIN 
    INSERT INTO [dbo].[Vat]   
        ([vatName]  
        ,[vatRate]  
        ,[vatSign]  
        ,[vatInclude]  
        ,[vatState])  
    VALUES 
        ('MwSt 8% exklusiv', 8, 'B', 0, 1),
        ('Steuerbefreit', 0, 'C', 0, 1),
        ('MwSt 2.5% exklusiv', 2.5, 'D', 0, 1),
        ('MwSt 8% inklusiv', 8, 'B1', 1, 1),
        ('MwSt 2.5% exklusiv', 2.5, 'D1', 1, 1)
 
END             

/****** ItemGroup ****/            
IF (select count(*) FROM [dbo].[ItemGroup]) = 0  
BEGIN 
    INSERT INTO [dbo].[ItemGroup]   
        ([itgNumber]  
        ,[itgName]  
        ,[itgState])  
    VALUES 
        (1, 'Hauptartikelgrupp', 1),
        (10, 'Dienstleistungen', 1),
        (20, 'Produkte', 1),
        (200, 'Nebenprodukte', 1),
        (100, 'Dienstleitungen Dritte', 1),
        (101, 'Projekte', 1)
 
END             
