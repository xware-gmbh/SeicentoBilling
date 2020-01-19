/***
 * Fallback Values for abacus 
 * 
 ***/

declare @CmpID bigint
select @CmpID = cmpId FROM Company WHERE cmpState = 1

declare @EntID bigint
select @EntID = entId FROM Entity WHERE entName = 'Company'

declare @ObjID bigint
select @ObjID = objId FROM RowObject WHERE objRowId=@CmpId and objentId = @EntId


/****** RowParameter ****/            
IF NOT EXISTS (SELECT 1 FROM [dbo].[RowParameter] WHERE [prmGroup] = 'abacus')  
BEGIN 
    INSERT INTO [dbo].[RowParameter]   
        ([prmObjId]
        ,[prmGroup]
        ,[prmSubGroup]  
        ,[prmKey]
        ,[prmValue]
        ,[prmValueType]
        ,[prmState])  
    VALUES 
        (@ObjID, 'abacus' ,'soap', 'defcreditaccount', '3000', 1,1),
        (@ObjID, 'abacus' ,'soap', 'drymode', 'true', 3,1),
        (@ObjID, 'abacus' ,'soap', 'maxrecords', '3', 1,1) 
END             
