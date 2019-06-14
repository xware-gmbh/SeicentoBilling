
/****** Object:  Table [dbo].[Activity]    Script Date: 06.03.2019 16:24:19 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Activity](
	[actId] [bigint] IDENTITY(1,1) NOT NULL,
	[actDate] [datetime] NOT NULL,
	[actType] [smallint] NOT NULL,
	[actcusId] [bigint] NOT NULL,
	[actText] [ntext] NULL,
	[actFollowingUpDate] [date] NULL,
	[actcsaId] [bigint] NULL,
	[actLink] [nvarchar](256) NULL,
	[actState] [smallint] NULL,
 CONSTRAINT [PK_Activity] PRIMARY KEY CLUSTERED 
(
	[actId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Activity]  WITH CHECK ADD  CONSTRAINT [FK_Activity_CostAccount] FOREIGN KEY([actcsaId])
REFERENCES [dbo].[CostAccount] ([csaId])
GO

ALTER TABLE [dbo].[Activity] CHECK CONSTRAINT [FK_Activity_CostAccount]
GO

ALTER TABLE [dbo].[Activity]  WITH CHECK ADD  CONSTRAINT [FK_Activity_Customer] FOREIGN KEY([actcusId])
REFERENCES [dbo].[Customer] ([cusId])
GO

ALTER TABLE [dbo].[Activity] CHECK CONSTRAINT [FK_Activity_Customer]
GO


/****** Object:  Table [dbo].[Address]    Script Date: 08.03.2019 11:07:34 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Address](
	[adrId] [bigint] IDENTITY(1,1) NOT NULL,
	[adrcusId] [bigint] NOT NULL,
	[adrIndex] [smallint] NOT NULL,
	[adrType] [smallint] NOT NULL,
	[adrLine0] [nvarchar](50) NULL,
	[adrLine1] [nvarchar](50) NULL,
	[adrZip] [nvarchar](50) NULL,
	[adrCity] [nvarchar](50) NULL,
	[adrValidFrom] [date] NULL,
	[adrRegion] [nvarchar](50) NULL,
	[adrCountry] [nvarchar](50) NULL,
	[adrRemark] [nvarchar](50) NULL,
	[adrName] [nvarchar](50) NULL,
	[adrAddOn] [nvarchar](50) NULL,
	[adrSalutation] [smallint] NULL,
	[adrState] [smallint] NOT NULL,
 CONSTRAINT [PK_Address] PRIMARY KEY CLUSTERED 
(
	[adrId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO

ALTER TABLE [dbo].[Address]  WITH CHECK ADD  CONSTRAINT [FK_Address_Customer] FOREIGN KEY([adrcusId])
REFERENCES [dbo].[Customer] ([cusId])
GO

ALTER TABLE [dbo].[Address] CHECK CONSTRAINT [FK_Address_Customer]
GO


/****** Object:  Table [dbo].[CustomerLink]    Script Date: 08.03.2019 11:06:10 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[CustomerLink](
	[cnkId] [bigint] IDENTITY(1,1) NOT NULL,
	[cnkcusId] [bigint] NOT NULL,
	[cnkIndex] [smallint] NOT NULL,
	[cnkType] [smallint] NOT NULL,
	[cnkLink] [nvarchar](256) NOT NULL,
	[cnkRemark] [nvarchar](50) NULL,
	[cnkValidFrom] [datetime] NULL,
	[cnkDepartment] [smallint] NULL,	
	[cnkState] [smallint] NOT NULL,
 CONSTRAINT [PK_CustomerLinks] PRIMARY KEY CLUSTERED 
(
	[cnkId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO

ALTER TABLE [dbo].[CustomerLink]  WITH CHECK ADD  CONSTRAINT [FK_CustomerLink_Customer] FOREIGN KEY([cnkcusId])
REFERENCES [dbo].[Customer] ([cusId])
GO

ALTER TABLE [dbo].[CustomerLink] CHECK CONSTRAINT [FK_CustomerLink_Customer]
GO


/****** Object:  Table [dbo].[LabelDefinition]    Script Date: 06.03.2019 16:33:32 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[LabelDefinition](
	[cldId] [bigint] IDENTITY(1,1) NOT NULL,
	[cldType] [smallint] NOT NULL,
	[cldText] [nvarchar](50) NOT NULL,
	[cldState] [smallint] NOT NULL,
 CONSTRAINT [PK_Label] PRIMARY KEY CLUSTERED 
(
	[cldId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO




/****** Object:  Table [dbo].[LabelAssignment]    Script Date: 06.03.2019 16:33:09 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[LabelAssignment](
	[claId] [bigint] IDENTITY(1,1) NOT NULL,
	[clacusId] [bigint] NOT NULL,
	[clacldId] [bigint] NOT NULL,
	[claIndex] [smallint] NULL,
 CONSTRAINT [PK_LabelAssignment] PRIMARY KEY CLUSTERED 
(
	[claId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[LabelAssignment]  WITH CHECK ADD  CONSTRAINT [FK_LabelAssignment_Customer] FOREIGN KEY([clacusId])
REFERENCES [dbo].[Customer] ([cusId])
GO

ALTER TABLE [dbo].[LabelAssignment] CHECK CONSTRAINT [FK_LabelAssignment_Customer]
GO

ALTER TABLE [dbo].[LabelAssignment]  WITH CHECK ADD  CONSTRAINT [FK_LabelAssignment_LabelDefinition] FOREIGN KEY([clacldId])
REFERENCES [dbo].[LabelDefinition] ([cldId])
GO

ALTER TABLE [dbo].[LabelAssignment] CHECK CONSTRAINT [FK_LabelAssignment_LabelDefinition]
GO


/****** Object:  Table [dbo].[Company]   Modify ******/
IF COL_LENGTH('[dbo].[Company]', 'cmpState') IS NULL
BEGIN
    ALTER TABLE [dbo].[Company]
    ADD 
	cmpState smallint null
END
GO

update [dbo].[Company] set cmpState = 1 where cmpId=1;
GO

/****** Object:  Table [dbo].[Project]   Modify ******/
IF COL_LENGTH('[dbo].[Project]', 'proContact') IS NULL
BEGIN
    ALTER TABLE [dbo].[Project]
    ADD 
    [proContact] nvarchar(40) NULL,
	[proadrId] [bigint] NULL;
	
	ALTER TABLE [dbo].[Project]  WITH CHECK ADD  CONSTRAINT [FK_Project_Address] FOREIGN KEY([proadrId])
	REFERENCES [dbo].[Address] ([adrId])

	ALTER TABLE [dbo].[Project] CHECK CONSTRAINT [FK_Project_Address]
END
GO

/****** Object:  Table [dbo].[ContactRelation]    Script Date: 26.04.2019 17:02:04 ******/

CREATE TABLE [dbo].[ContactRelation](
	[corId] [bigint] IDENTITY(1,1) NOT NULL,
	[corTypeOne] [smallint] NULL,
	[corcusIdTypeOne] [bigint] NULL,
	[corTypeTwo] [smallint] NULL,
	[corcusIdTypeTwo] [bigint] NULL,
	[corRemark] [nvarchar](50) NULL,
 CONSTRAINT [PK_ContactRelation] PRIMARY KEY CLUSTERED 
(
	[corId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[ContactRelation]  WITH CHECK ADD  CONSTRAINT [FK_ContactRelation_CustomerOne] FOREIGN KEY([corcusIdTypeOne])
REFERENCES [dbo].[Customer] ([cusId])
GO

ALTER TABLE [dbo].[ContactRelation] CHECK CONSTRAINT [FK_ContactRelation_CustomerOne]
GO

ALTER TABLE [dbo].[ContactRelation]  WITH CHECK ADD  CONSTRAINT [FK_ContactRelation_CustomerTwo] FOREIGN KEY([corcusIdTypeTwo])
REFERENCES [dbo].[Customer] ([cusId])
GO

ALTER TABLE [dbo].[ContactRelation] CHECK CONSTRAINT [FK_ContactRelation_CustomerTwo]
GO


/****** Entity ****/            
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

/****** LabelDefinition ****/            
IF NOT EXISTS (SELECT 1 FROM [dbo].[LabelDefinition] WHERE [cldText] = 'Kunde')  
BEGIN 
    INSERT INTO [dbo].[LabelDefinition]   
        ([cldType]  
        ,[cldText]  
        ,[cldState])  
    VALUES 
     (1, 'Weihnachtskarte' ,1),
     (1, 'Mitarbeiter' ,1),
     (1, 'Lieferant' ,1),
     (1, 'Lead' ,1),
     (1, 'Kunde' ,1)
 
END

