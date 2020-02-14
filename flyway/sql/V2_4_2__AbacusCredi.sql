--

/****** Object: Company : abacusparam ***/
IF COL_LENGTH('[dbo].[Company]', 'cmpAbaEndpointCre') IS NULL
BEGIN
    ALTER TABLE [dbo].[Company]
    ADD 
	cmpAbaEndpointCre     nvarchar(256) NULL,
	cmpAbaEndpointCreDoc  nvarchar(256) NULL;
END

/****** Object: Item : Account# ***/
IF COL_LENGTH('[dbo].[Item]', 'itmAccount') IS NULL
BEGIN
    ALTER TABLE [dbo].[Item]
    ADD 
	itmAccount  numeric(10, 3) NULL;
END
