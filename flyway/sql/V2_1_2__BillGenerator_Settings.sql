/****** Set Defaults for Billing ***/
declare @CmpID bigint
select @CmpID = cmpId FROM Company WHERE cmpState = 1

declare @EntID bigint
select @EntID = entId FROM Entity WHERE entName = 'Company'

declare @ObjId bigint
select @ObjId = objId from RowObject where objRowId = @CmpID and objentId = @EntID

IF NOT EXISTS (SELECT 1 FROM [dbo].[RowParameter] WHERE prmobjId = @ObjId and prmGroup = 'billing' and prmSubGroup = 'generator')  
BEGIN 
    INSERT INTO [dbo].[RowParameter]   
        ([prmobjId]  
        ,[prmGroup]  
        ,[prmSubGroup]
        ,[prmKey]
        ,[prmValue]
        ,[prmValueType]
        ,[prmState])  
    VALUES 
        (@ObjId,'billing', 'generator' , 'headerText', 'Gemäss Projektauftrag {proExtReference}  Name: {proName}', 0,1),
        (@ObjId,'billing', 'generator' , 'lineTextProject', 'Dienstleistung {csaCode} gemäss beigelegtem Rapport', 0,1),
        (@ObjId,'billing', 'generator' , 'itemIdentProject', '2000', 0,1),
        (@ObjId,'billing', 'generator' , 'lineTextExpense', 'Spesen {csaName}', 0,1),
        (@ObjId,'billing', 'generator' , 'itemIdentExpense', '2102', 0,1),
        (@ObjId,'billing', 'generator' , 'lineTextJourney', 'Reisezeit {csaName} gemäss Rapport', 0,1),
        (@ObjId,'billing', 'generator' , 'itemIdentJourney', '2100', 0,1)
 
END             
