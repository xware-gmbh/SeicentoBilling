
/****** Initial Value für Bank ******/
IF NOT EXISTS (SELECT 1 FROM [dbo].[Bank] WHERE [bnkState] = 1)  
BEGIN 
    INSERT INTO [dbo].[Bank]   
        ([bnkName]  
        ,[bnkAddress]  
        ,[bnkState]
        )  
    VALUES 
        ('Demo', 'Gartenweg' ,1)
 
END             

/****** falscher Werttyp in Tabelle ******/
ALTER TABLE [dbo].[ExpenseTemplate]  
	ALTER COLUMN extFlagGeneric smallint;

/****** Feld mit Funktion entfernen auf Project ******/
ALTER TABLE [dbo].[Project]
    DROP COLUMN proHoursEffective

ALTER TABLE [dbo].[Project]
	ADD proHoursEffective decimal(18,2)
	
/**  
 *   Stored Procedure 
 * 
 * **/
-- =============================================
-- Author:		Muri Josef
-- Create date: 07.07.2019
-- Description:	Stored Proc - Berechnen Ist Stunden auf Projekt
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE [name] = 'seicento_Calculate_ProjectHours' AND [type] = 'P')
      DROP PROCEDURE  dbo.seicento_Calculate_ProjectHours
GO
	
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE seicento_Calculate_ProjectHours 
	-- Add the parameters for the stored procedure here
	@ProjectID bigint = 0
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
    DECLARE @ret dec(18,2);

    SELECT @ret = SUM(p.prlHours) FROM dbo.ProjectLine p
        WHERE p.prlproId = @ProjectID
           AND p.prlState = 1 and p.prlWorkType < 4;

    IF (@ret IS NULL) SET @ret = 0.00;

	--DISABLE TRIGGER tr_seicento_postSaveProject ON Project;

	update dbo.Project set proHoursEffective = @ret where proId = @ProjectID;

	--ENABLE TRIGGER tr_seicento_postSaveProject ON Project;  
    
END
GO

-- =============================================
-- Author:		Muri Josef
-- Create date: 07.07.2019
-- Description:	Loop über alle Projekte
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE [name] = 'seicento_CalculateAll_ProjectHours' AND [type] = 'P')
      DROP PROCEDURE  dbo.seicento_CalculateAll_ProjectHours
GO
	
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE seicento_CalculateAll_ProjectHours 
	-- Add the parameters for the stored procedure here
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	declare @field1 int
	declare cur CURSOR LOCAL for
	    select proId from Project
	
	open cur
	
	fetch next from cur into @field1

	while @@FETCH_STATUS = 0 BEGIN

    	--execute your sproc on each row
		EXEC [dbo].[seicento_Calculate_ProjectHours] @field1

    	fetch next from cur into @field1
	END

	close cur
	deallocate cur    
END
GO

-- =============================================
-- Author:		Muri Josef
-- Create date: 07.07.2019
-- Description:	Trigger für Save
-- =============================================

IF EXISTS (SELECT * FROM sys.objects WHERE [name] = 'tr_seicento_postSaveProject' AND [type] = 'TR')
      DROP TRIGGER  dbo.tr_seicento_postSaveProject
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE TRIGGER dbo.tr_seicento_postSaveProject
   ON  dbo.Project 
   AFTER INSERT, UPDATE
AS 
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for trigger here
	DECLARE	@return_value int

	declare @p1 bigint
	Select @p1 =  proId FROM INSERTED

	EXEC @return_value = [dbo].[seicento_Calculate_ProjectHours]
	@ProjectID = @p1

END
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

CREATE TRIGGER dbo.tr_seicento_postSavePLine
   ON  dbo.ProjectLine 
   AFTER UPDATE
AS 
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for trigger here
	DECLARE	@return_value int

	declare @p1 bigint
	Select @p1 =  prlproId FROM INSERTED

	EXEC @return_value = [dbo].[seicento_Calculate_ProjectHours]
	@ProjectID = @p1

END
GO


-- intivalues
EXEC [dbo].[seicento_CalculateAll_ProjectHours]
GO
