/***
 * Signoff für Spesen TFS Values for abacus TFS272
 * 
 ***/

/****** Object: Company : abacusparam ***/
IF COL_LENGTH('[dbo].[Company]', 'cmpAbaEndpointPay') IS NULL
BEGIN
    ALTER TABLE [dbo].[Company]
    ADD 
	cmpAbaEndpointPay  nvarchar(256) NULL;	
END

GO

IF EXISTS (SELECT 1 FROM [dbo].[Company] WHERE [cmpAbaEndpointPay] is null and cmpState = 1)  
BEGIN 
	update [dbo].[Company] set cmpAbaEndpointPay = 'https://abatreuhand.truvag.ch/abaconnect/services/Payment_2013_00' where cmpState = 1;
END


