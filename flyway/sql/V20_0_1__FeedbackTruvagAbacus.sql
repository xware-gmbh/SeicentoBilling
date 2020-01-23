/***
 * Fallback Values for abacus 
 * 
 ***/

IF COL_LENGTH('[dbo].[Vat]', 'vatExtRef1') IS NULL
BEGIN
    ALTER TABLE [dbo].[Vat]
    ADD 
	vatExtRef1  nvarchar(20) NULL;
END

IF COL_LENGTH('[dbo].[PaymentCondition]', 'pacExtRef1') IS NULL
BEGIN
    ALTER TABLE [dbo].[PaymentCondition]
    ADD 
	pacExtRef1  nvarchar(20) NULL;
END
