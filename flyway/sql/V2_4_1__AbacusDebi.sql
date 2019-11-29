--
/****** Object: CostAccount : ExtRef ***/
IF COL_LENGTH('[dbo].[CostAccount]', 'csaExtRef') IS NULL
BEGIN
    ALTER TABLE [dbo].[CostAccount]
    ADD 
	csaExtRef  nvarchar(20) NULL;
END
/****** Object: Vat : ExtRef ***/
IF COL_LENGTH('[dbo].[Vat]', 'vatExtRef') IS NULL
BEGIN
    ALTER TABLE [dbo].[Vat]
    ADD 
	vatExtRef  nvarchar(20) NULL;
END
/****** Object: Customer : ExtRef ***/
IF COL_LENGTH('[dbo].[Customer]', 'cusExtRef1') IS NULL
BEGIN
    ALTER TABLE [dbo].[Customer]
    ADD 
	cusExtRef1  nvarchar(20) NULL,
	cusExtRef2  nvarchar(20) NULL;
END

/****** Object: Company : abacusparam ***/
IF COL_LENGTH('[dbo].[Company]', 'cmpAbaActive') IS NULL
BEGIN
    ALTER TABLE [dbo].[Company]
    ADD 
	cmpAbaActive       bit NULL,
	cmpAbaEndpointCus  nvarchar(256) NULL,
	cmpAbaEndpointDoc  nvarchar(256) NULL,
	cmpAbaUser         nvarchar(20) NULL,
	cmpAbaMandator     int NULL,
	cmpAbaMaxDays      int NULL;
END
