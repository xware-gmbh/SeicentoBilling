

/****** Object:  Table [dbo].[VatLine]    Script Date: 12.07.2019 09:12:45 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = N'VatLine')
BEGIN

	CREATE TABLE [dbo].[VatLine](
	[vanId] [bigint] IDENTITY(1,1) NOT NULL,
	[vanValidFrom] [date] NOT NULL,
	[vanvatId] [bigint] NOT NULL,
	[vanRate] [numeric](6, 4) NOT NULL,
	[vanRemark] [nvarchar](50) NULL,
	[vanState] [smallint] NOT NULL,
 CONSTRAINT [PK_VatLine] PRIMARY KEY CLUSTERED 
(
	[vanId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

ALTER TABLE [dbo].[VatLine]  WITH CHECK ADD  CONSTRAINT [FK_VatLine_Vat] FOREIGN KEY([vanvatId])
REFERENCES [dbo].[Vat] ([vatId])

ALTER TABLE [dbo].[VatLine] CHECK CONSTRAINT [FK_VatLine_Vat]
	
END
GO



/****** Add to Entity ****/            
IF NOT EXISTS (SELECT 1 FROM [dbo].[Entity] WHERE [entname] = 'VatLine')  
BEGIN 
    INSERT INTO [dbo].[Entity]   
        ([entName]  
        ,[entAbbreviation]  
        ,[entHasrowobject]
        ,[entState])  
    VALUES 
        ('VatLine', 'van' ,1,1)
 
END             
