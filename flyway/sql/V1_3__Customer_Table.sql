/****** Object:  Table [dbo].[Customer]   Erweiterung 1 um ein paar Felder (Februrar 2019) ******/

IF COL_LENGTH('[dbo].[Customer]', 'cusAccountType') IS NULL
BEGIN
    ALTER TABLE [dbo].[Customer]
    ADD cusWebSite nvarchar(40),
	cusPhone1 nvarchar(40),
	cusPhone2 nvarchar(40),
	cusEmail1 nvarchar(40),
	cusEmail2 nvarchar(40),
	cusFlagDebtor smallint,
	cusFlagCreditor smallint,
	cusFlagEmployee smallint,
	cusFlagPromotion smallint,
	cusAccountManager nvarchar(40),
	cusAccountType smallint;
	
END

IF COL_LENGTH('[dbo].[Customer]', 'cusSalutation') IS NULL
BEGIN
    ALTER TABLE [dbo].[Customer]
    ADD 
	cusSalutation smallint,
	cusBirthdate datetime NULL,
	cusBillingTarget smallint NULL,
	cusBillingReport smallint NULL;
	
END

IF COL_LENGTH('[dbo].[Customer]', 'cusWebSite') IS NOT NULL
BEGIN
    ALTER TABLE [dbo].[Customer]
    drop column cusWebSite,
	cusPhone1,
	cusPhone2,
	cusEmail1,
	cusEmail2,
	cusFlagDebtor,
	cusFlagCreditor,
	cusFlagEmployee,
	cusFlagPromotion;
	
END