/****** Object:  Table [dbo].[ExpenseTemplate]    Script Date: 06.03.2019 17:45:03 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[ExpenseTemplate](
	[extId] [bigint] IDENTITY(1,1) NOT NULL,
	[extKeyNumber] [int] NOT NULL,
	[extcsaId] [bigint] NOT NULL,
	[extproId] [bigint] NOT NULL,
	[extAccount] [nvarchar](50) NULL,
	[extFlagCostAccount] [bit] NULL,
	[extFlagGeneric] [bit] NULL,
	[extvatId] [bigint] NULL,
	[extText] [nvarchar](128) NULL,
	[extUnit] [smallint] NULL,
	[extQuantity] [decimal](6, 2) NULL,
	[extAmount] [decimal](6, 2) NOT NULL,
	[extState] [smallint] NULL,
 CONSTRAINT [PK_ExpenseTemplate] PRIMARY KEY CLUSTERED 
(
	[extId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[ExpenseTemplate]  WITH CHECK ADD  CONSTRAINT [FK_ExpenseTemplate_CostAccount] FOREIGN KEY([extcsaId])
REFERENCES [dbo].[CostAccount] ([csaId])
GO

ALTER TABLE [dbo].[ExpenseTemplate] CHECK CONSTRAINT [FK_ExpenseTemplate_CostAccount]
GO

ALTER TABLE [dbo].[ExpenseTemplate]  WITH CHECK ADD  CONSTRAINT [FK_ExpenseTemplate_ExpenseTemplate] FOREIGN KEY([extId])
REFERENCES [dbo].[ExpenseTemplate] ([extId])
GO

ALTER TABLE [dbo].[ExpenseTemplate] CHECK CONSTRAINT [FK_ExpenseTemplate_ExpenseTemplate]
GO

ALTER TABLE [dbo].[ExpenseTemplate]  WITH CHECK ADD  CONSTRAINT [FK_ExpenseTemplate_Vat] FOREIGN KEY([extvatId])
REFERENCES [dbo].[Vat] ([vatId])
GO

ALTER TABLE [dbo].[ExpenseTemplate] CHECK CONSTRAINT [FK_ExpenseTemplate_Vat]
GO

/****** Object:  Table [dbo].[ProjectLineTemplate]    Script Date: 06.03.2019 17:48:16 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[ProjectLineTemplate](
	[prtId] [bigint] IDENTITY(1,1) NOT NULL,
	[prtcsaId] [bigint] NOT NULL,
	[prtKeyNumber] [int] NOT NULL,
	[prtproId] [bigint] NULL,
	[prtHours] [decimal](6, 2) NULL,
	[prtText] [nvarchar](384) NULL,
	[prtRate] [decimal](6, 2) NULL,
	[prtWorkType] [smallint] NULL,
	[prtState] [smallint] NOT NULL,
 CONSTRAINT [PK_ProjectLineTemplate] PRIMARY KEY CLUSTERED 
(
	[prtId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[ProjectLineTemplate]  WITH CHECK ADD  CONSTRAINT [FK_ProjectLineTemplate_CostAccount] FOREIGN KEY([prtcsaId])
REFERENCES [dbo].[CostAccount] ([csaId])
GO

ALTER TABLE [dbo].[ProjectLineTemplate] CHECK CONSTRAINT [FK_ProjectLineTemplate_CostAccount]
GO

ALTER TABLE [dbo].[ProjectLineTemplate]  WITH CHECK ADD  CONSTRAINT [FK_ProjectLineTemplate_Project] FOREIGN KEY([prtproId])
REFERENCES [dbo].[Project] ([proId])
GO

ALTER TABLE [dbo].[ProjectLineTemplate] CHECK CONSTRAINT [FK_ProjectLineTemplate_Project]
GO


