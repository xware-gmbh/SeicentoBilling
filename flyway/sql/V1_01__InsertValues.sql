/****** Entity ****/            
IF NOT EXISTS (SELECT 1 FROM [dbo].[Entity] WHERE [entname] = 'Bank')  
BEGIN 
    INSERT INTO [dbo].[Entity]   
        ([entName]  
        ,[entAbbreviation]  
        ,[entHasrowobject]
        ,[entState])  
    VALUES 
        ('Bank', 'bnk' ,1,1),
        ('City', 'cty' ,1,1),
        ('Company', 'cmp' ,1,1),
        ('Customer', 'cus' ,1,1),
        ('DatabaseVersion', 'dbv' ,1,1),
        ('Entity', 'ent' ,1,1),
        ('Item', 'itm' ,1,1),
        ('ItemGroup', 'itg' ,1,1),
        ('Language', 'lng' ,1,1),
        ('Order', 'ord' ,1,1),
        ('OrderLine', 'odl' ,1,1),
        ('RowImage', '' ,1,1),
        ('RowLabel', '' ,1,1),
        ('RowObject', '' ,0,1),
        ('RowParameter', '' ,1,1),
        ('RowRelation', '' ,1,1),
        ('RowSecurity', '' ,1,1),
        ('RowText', '' ,1,1),
        ('StateCode', '' ,1,1),
        ('Vat', 'vat' ,1,1),
        ('Expense', 'exp' ,1,1),
        ('Project', 'pro' ,1,1),
        ('ProjectLine', 'prl' ,1,1),
        ('Periode', 'per' ,1,1),
        ('PaymentCondition', 'pac' ,1,1),
        ('ResPlanning', 'rsp' ,1,1),
        ('CostAccount', 'csa' ,1,1)
 
END             


IF NOT EXISTS (SELECT 1 FROM [dbo].[Entity] WHERE [entname] = 'Activity')  
BEGIN 
    INSERT INTO [dbo].[Entity]   
        ([entName]  
        ,[entAbbreviation]  
        ,[entHasrowobject]
        ,[entState])  
    VALUES 
        ('Activity', 'act' ,1,1),
        ('Address', 'adr' ,1,1),
        ('CustomerLink', 'cnk' ,1,1),
        ('LabelAssignment', 'cla' ,1,1),
        ('LabelDefinition', 'cld' ,1,1),
        ('ContactRelation', 'cor' ,1,1)
 
END             

IF NOT EXISTS (SELECT 1 FROM [dbo].[Entity] WHERE [entname] = 'ProjectLineTemplate')  
BEGIN 
    INSERT INTO [dbo].[Entity]   
        ([entName]  
        ,[entAbbreviation]  
        ,[entHasrowobject]
        ,[entState])  
    VALUES 
        ('ProjectLineTemplate', 'prt' ,1,1),
        ('ExpenseTemplate', 'ext' ,1,1)
 
END 


/****** Language ****/            
IF NOT EXISTS (SELECT 1 FROM [dbo].[Language] WHERE [lngCode] = 1)  
BEGIN 
    INSERT INTO [dbo].[Language]   
        ([lngCode]  
        ,[lngName]  
        ,[lngIsocode]  
        ,[lngKeyboard]  
        ,[lngDefault]  
        ,[lngState])  
    VALUES 
     (1, 'Deutsch', 'CH', 'de_ch', 1 ,1)
 
END

/****** Company ****/            
IF NOT EXISTS (SELECT 1 FROM [dbo].[Company] WHERE [cmpState] = 1)  
BEGIN 
    INSERT INTO [dbo].[Company]   
        ([cmpName]  
        ,[cmpState])  
    VALUES 
     ('Demo Firma', 1)
 
END
