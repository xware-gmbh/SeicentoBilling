/****** Object: Customer: Single PDF ***/
IF COL_LENGTH('[dbo].[Customer]', 'cusSinglePdf') IS NULL
BEGIN
    ALTER TABLE [dbo].[Customer]
    ADD 
--	cusBirthdate datetime NULL,
	cusSinglePdf bit NULL;
	
END

/****** Object: Project: ***/
IF COL_LENGTH('[dbo].[Project]', 'proInternal') IS NULL
BEGIN
    ALTER TABLE [dbo].[Project]
    ADD 
--	cusBirthdate datetime NULL,
	proInternal bit NULL;
	
END