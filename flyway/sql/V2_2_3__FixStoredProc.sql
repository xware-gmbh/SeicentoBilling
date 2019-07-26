-- =============================================
-- Author:		Muri Josef
-- Create date: 07.07.2019
-- Description:	Alte Skalar entfernen
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE [name] = 'ufnGetGetReportingHours' AND [type] = 'FN')
      DROP FUNCTION  dbo.ufnGetGetReportingHours
GO


-- =============================================
-- Author:		Muri Josef
-- Create date: 07.07.2019
-- Description:	Trigger für Save ProjectLine
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE [name] = 'tr_seicento_postSavePLine' AND [type] = 'TR')
      DROP TRIGGER  dbo.tr_seicento_postSavePLine
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TRIGGER [dbo].[tr_seicento_postSavePLine]
   ON  [dbo].[ProjectLine] 
   AFTER INSERT,DELETE,UPDATE
AS 
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for trigger here
	DECLARE	@return_value int
	declare @p1 bigint

	-- update
	IF EXISTS (SELECT * FROM inserted) AND EXISTS (SELECT * FROM deleted)
	BEGIN
		Select @p1 =  prlproId FROM INSERTED
	END
	-- insert
	IF EXISTS (SELECT * FROM inserted) AND NOT EXISTS(SELECT * FROM deleted)
	BEGIN
		Select @p1 =  prlproId FROM INSERTED
	END
	-- delete
	IF EXISTS (SELECT * FROM deleted) AND NOT EXISTS(SELECT * FROM inserted)
	BEGIN
		Select @p1 =  prlproId FROM deleted
	END


	EXEC @return_value = [dbo].[seicento_Calculate_ProjectHours]
	@ProjectID = @p1

END



