/***
 * Signoff für Spesen TFS Values for abacus TFS272
 * 
 ***/

IF COL_LENGTH('[dbo].[Periode]', 'perSignOffExpense') IS NULL
BEGIN
    ALTER TABLE [dbo].[Periode]
    ADD 
	perSignOffExpense  bit NULL;

END

