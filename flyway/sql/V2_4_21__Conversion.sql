

/****** Object:  Table [dbo].[Conversion]    Script Date: 12.07.2019 09:12:45 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = N'Conversion')
BEGIN

	CREATE TABLE [dbo].[Conversion](
	[cnvId] [bigint] IDENTITY(1,1) NOT NULL,
	[cnvGroup] [nvarchar](40) NOT NULL,
	[cnvSubGroup] [nvarchar](40) NOT NULL,
	[cnvValueIn] [nvarchar](80) NOT NULL,
	[cnvValueOut] [nvarchar](80) NULL,
	[cnvRemark] [nvarchar](80) NOT NULL,
	[cnvDataType] [smallint] NOT NULL,
	[cnvState] [smallint] NOT NULL,
 CONSTRAINT [PK_Conversion] PRIMARY KEY CLUSTERED 
(
	[cnvId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

	
END
GO

/****** Add to Entity ****/            
IF NOT EXISTS (SELECT 1 FROM [dbo].[Entity] WHERE [entname] = 'Conversion')  
BEGIN 
    INSERT INTO [dbo].[Entity]   
        ([entName]  
        ,[entAbbreviation]  
        ,[entHasrowobject]
        ,[entState])  
    VALUES 
        ('Conversion', 'cnv' ,0,1)
 
END             
