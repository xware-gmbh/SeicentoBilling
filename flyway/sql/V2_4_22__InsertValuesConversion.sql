/***
 * Allgemeine Umwandlungstabelle 
 * 
 **/

/****** Conversion of Expensetypes to account# ****/            
IF NOT EXISTS (SELECT 1 FROM [dbo].[Conversion] WHERE [cnvGroup] = 'account')  
BEGIN 
    INSERT INTO [dbo].[Conversion]   
        ([cnvGroup]  
        ,[cnvSubGroup]  
        ,[cnvValueIn]
        ,[cnvValueOut]
        ,[cnvRemark]
        ,[cnvDataType]
        ,[cnvState])  
    VALUES 
        ('account', 'abacus' ,'a.Spesen', '4700', '', 1,1),
        ('account', 'abacus' ,'a.Weiterbildung', '5810', '', 1,1),
        ('account', 'abacus' ,'a.Büroaufwand', '4000', '', 1,1),
        ('account', 'abacus' ,'a.Reisespesen', '6640', '', 1,1),
        ('account', 'abacus' ,'a.Repräsentation', '6640', '', 1,1),
        ('account', 'abacus' ,'a.Werbung / Marketing', '6600', '', 1,1),
        ('account', 'abacus' ,'a.Mietaufwand', '6000', '', 1,1),
        ('account', 'abacus' ,'a.EDV Unterhalt', '6120', '', 1,1)
 
END

IF NOT EXISTS (SELECT 1 FROM [dbo].[Conversion] WHERE [cnvGroup] = 'paymentcnd')  
BEGIN 
    INSERT INTO [dbo].[Conversion]   
        ([cnvGroup]  
        ,[cnvSubGroup]  
        ,[cnvValueIn]
        ,[cnvValueOut]
        ,[cnvRemark]
        ,[cnvDataType]
        ,[cnvState])  
    VALUES 
        ('paymentcnd', 'abacus' ,'30N', '1', '', 1,1),
        ('paymentcnd', 'abacus' ,'60N', '1', '', 1,1),
        ('paymentcnd', 'abacus' ,'10N', '1', '', 1,1),
        ('paymentcnd', 'abacus' ,'20N', '6', '', 1,1),
        ('paymentcnd', 'abacus' ,'5N', '1', '', 1,1),
        ('paymentcnd', 'abacus' ,'1N', '1', '', 1,1)
 
END             

