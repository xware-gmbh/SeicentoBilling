/***
 * Feedback based on Test with Christian Klauenbösch 
 * 	Rowobject does not exist on inital DB.... create dummy!
 * 
 ***/

declare @CmpID bigint
select @CmpID = cmpId FROM Company WHERE cmpState = 1

declare @EntID bigint
select @EntID = entId FROM Entity WHERE entName = 'Company'

IF NOT EXISTS (SELECT 1 FROM [dbo].[RowObject] WHERE objentId = @EntID and objRowId = @CmpId)  
BEGIN 
    INSERT INTO [dbo].[RowObject]   
        ([objentId]  
        ,[objRowId]  
        ,[objState])  
    VALUES 
        (@ENTId, @CmpID, 1)
 
END             

