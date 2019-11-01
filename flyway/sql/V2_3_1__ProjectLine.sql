/****** Object: ProjectLine : Zeit von/bis ***/
IF COL_LENGTH('[dbo].[ProjectLine]', 'prlTimeFrom') IS NULL
BEGIN
    ALTER TABLE [dbo].[ProjectLine]
    ADD 
	prlTimeFrom datetime NULL,
	prlTimeTo datetime NULL;
	
END

