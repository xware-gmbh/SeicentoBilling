

/****** Object:  UserDefinedFunction [dbo].[ufnGetGetReportingHours]    Script Date: 06.03.2019 18:09:14 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

/* If we already exist, get rid of us, and fix our spelling */
IF OBJECT_ID('dbo.ufnGetGetReportingHours') IS NOT NULL
  DROP FUNCTION ufnGetGetReportingHours
GO


CREATE FUNCTION [dbo].[ufnGetGetReportingHours](@ProjectID int)
RETURNS dec(18,2)
AS
-- Returns the stock level for the product.
BEGIN
    DECLARE @ret dec(18,2);
    SELECT @ret = SUM(p.prlHours)
    FROM dbo.ProjectLine p
    WHERE p.prlproId = @ProjectID
        AND p.prlState = 1 and p.prlWorkType < 4;
     IF (@ret IS NULL)
        SET @ret = 0.00;
    RETURN @ret;
END;
GO


/****** Object:  Table [dbo].[Vat]    Script Date: 06.03.2019 17:43:19 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Vat](
	[vatId] [bigint] IDENTITY(1,1) NOT NULL,
	[vatName] [nvarchar](40) NULL,
	[vatRate] [numeric](6, 4) NULL,
	[vatSign] [nvarchar](5) NULL,
	[vatInclude] [bit] NULL,
	[vatState] [smallint] NULL,
 CONSTRAINT [PK__Vat__429329001A14E395] PRIMARY KEY CLUSTERED 
(
	[vatId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Vat] ADD  CONSTRAINT [DF_Vat_vatInclude]  DEFAULT ((0)) FOR [vatInclude]
GO


/****** Object:  Table [dbo].[Entity]    Script Date: 06.03.2019 17:33:09 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Entity](
	[entId] [bigint] IDENTITY(0,1) NOT NULL,
	[entName] [nvarchar](40) NULL,
	[entAbbreviation] [nvarchar](6) NULL,
	[entDataclass] [nvarchar](256) NULL,
	[entHasrowobject] [bit] NULL,
	[entReadonly] [bit] NULL,
	[entExport2sdf] [bit] NULL,
	[entSdfOrdinal] [smallint] NULL,
	[entAuditHistory] [smallint] NULL,
	[entType] [int] NULL,
	[entState] [smallint] NULL,
 CONSTRAINT [PK_Entity] PRIMARY KEY NONCLUSTERED 
(
	[entId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO


/****** Object:  Table [dbo].[PaymentCondition]    Script Date: 06.03.2019 17:16:45 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[PaymentCondition](
	[pacId] [bigint] IDENTITY(1,1) NOT NULL,
	[pacCode] [nvarchar](5) NULL,
	[pacName] [nvarchar](50) NULL,
	[pacNbrOfDays] [int] NOT NULL,
	[pacState] [smallint] NULL,
 CONSTRAINT [PK_PaymentCondition] PRIMARY KEY CLUSTERED 
(
	[pacId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[PaymentCondition] ADD  CONSTRAINT [DF_PaymentCondition_pacNbrOfDays]  DEFAULT ((0)) FOR [pacNbrOfDays]
GO

/****** Object:  Table [dbo].[Company]    Script Date: 06.03.2019 17:40:53 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Company](
	[cmpId] [bigint] IDENTITY(1,1) NOT NULL,
	[cmpName] [nvarchar](40) NULL,
	[cmpAddress] [nvarchar](40) NULL,
	[cmpZip] [int] NULL,
	[cmpPlace] [nvarchar](40) NULL,
	[cmpVatcode] [nvarchar](50) NULL,
	[cmpCurrency] [nvarchar](5) NULL,
	[cmpUid] [nvarchar](50) NULL,
	[cmpPhone] [nvarchar](50) NULL,
	[cmpMail] [nvarchar](50) NULL,
	[cmpComm1] [nvarchar](50) NULL,
	[cmpBusiness] [nvarchar](50) NULL,
	[cmpLogo] [image] NULL,
	[cmpJasperUri] [nchar](256) NULL,
	[cmpBookingYear] [int] NULL,
	[cmpLastOrderNbr] [int] NULL,
	[cmpLastItemNbr] [int] NULL,
	[cmpLastCustomerNbr] [int] NULL,
	[cmpReportUsr] [nvarchar](20) NULL,
	[cmpReportPwd] [nvarchar](50) NULL,
	[cmpState] smallint NULL,
 CONSTRAINT [PK__Company__745E20780425A276] PRIMARY KEY CLUSTERED 
(
	[cmpId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

ALTER TABLE [dbo].[Company] ADD  CONSTRAINT [DF__Company__cmpBook__060DEAE8]  DEFAULT ((2012)) FOR [cmpBookingYear]
GO

ALTER TABLE [dbo].[Company] ADD  CONSTRAINT [DF__Company__cmpLast__07020F21]  DEFAULT ((1)) FOR [cmpLastOrderNbr]
GO

ALTER TABLE [dbo].[Company] ADD  CONSTRAINT [DF__Company__cmpLast__07F6335A]  DEFAULT ((1)) FOR [cmpLastItemNbr]
GO




/****** Object:  Table [dbo].[City]    Script Date: 06.03.2019 17:15:55 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[City](
	[ctyId] [bigint] IDENTITY(1,1) NOT NULL,
	[ctyName] [nvarchar](40) NOT NULL,
	[ctyCountry] [nvarchar](12) NULL,
	[ctyRegion] [nvarchar](20) NULL,
	[ctyGeoCoordinates] [nvarchar](20) NULL,
	[ctyZIP] [int] NULL,
	[ctyState] [smallint] NULL,
 CONSTRAINT [PK__City__25391DAB0AD2A005] PRIMARY KEY CLUSTERED 
(
	[ctyId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[Bank]    Script Date: 06.03.2019 17:40:18 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Bank](
	[bnkId] [bigint] IDENTITY(1,1) NOT NULL,
	[bnkName] [nvarchar](40) NULL,
	[bnkAddress] [nvarchar](60) NULL,
	[bnkctyId] [bigint] NULL,
	[bnkAccount] [nvarchar](60) NULL,
	[bnkIban] [nvarchar](60) NULL,
	[bnkState] [smallint] NULL,
	[bnkEsrTn] [nchar](20) NULL,
	[bnkCustomernbr] [bigint] NULL,
 CONSTRAINT [PK__Bank__C1EDFD34108B795B] PRIMARY KEY CLUSTERED 
(
	[bnkId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Bank]  WITH CHECK ADD  CONSTRAINT [fk_bank_city] FOREIGN KEY([bnkctyId])
REFERENCES [dbo].[City] ([ctyId])
GO

ALTER TABLE [dbo].[Bank] CHECK CONSTRAINT [fk_bank_city]
GO



/****** Object:  Table [dbo].[Customer]    Script Date: 06.03.2019 17:15:02 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Customer](
	[cusId] [bigint] IDENTITY(1,1) NOT NULL,
	[cusNumber] [int] NOT NULL,
	[cusName] [nvarchar](40) NOT NULL,
	[cusFirstName] [nvarchar](40) NULL,
	[cusCompany] [nvarchar](40) NULL,
	[cusAddress] [nvarchar](40) NULL,
	[cusLastContact] [datetime] NULL,
	[cusInfo] [ntext] NULL,
	[cusctyId] [bigint] NULL,
	[cuspacId] [bigint] NOT NULL,
	[cusState] [smallint] NULL,
	[cusLastBill] [date] NULL,
	[cusWebSite] [nvarchar](40) NULL,
	[cusPhone1] [nvarchar](40) NULL,
	[cusPhone2] [nvarchar](40) NULL,
	[cusEmail1] [nvarchar](40) NULL,
	[cusEmail2] [nvarchar](40) NULL,
	[cusFlagDebtor] [smallint] NULL,
	[cusFlagCreditor] [smallint] NULL,
	[cusFlagEmployee] [smallint] NULL,
	[cusFlagPromotion] [smallint] NULL,
	[cusAccountManager] [nvarchar](1) NULL,
	[cusAccountType] [smallint] NULL,
 CONSTRAINT [PK__Customer__BA9897F31DE57479] PRIMARY KEY CLUSTERED 
(
	[cusId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

ALTER TABLE [dbo].[Customer] ADD  CONSTRAINT [DF__Customer__cusPay__20C1E124]  DEFAULT ((1)) FOR [cuspacId]
GO

ALTER TABLE [dbo].[Customer]  WITH CHECK ADD  CONSTRAINT [FK_Customer_City] FOREIGN KEY([cusctyId])
REFERENCES [dbo].[City] ([ctyId])
GO

ALTER TABLE [dbo].[Customer] CHECK CONSTRAINT [FK_Customer_City]
GO

ALTER TABLE [dbo].[Customer]  WITH CHECK ADD  CONSTRAINT [FK_Customer_PaymentCondition] FOREIGN KEY([cuspacId])
REFERENCES [dbo].[PaymentCondition] ([pacId])
GO

ALTER TABLE [dbo].[Customer] CHECK CONSTRAINT [FK_Customer_PaymentCondition]
GO


/****** Object:  Table [dbo].[CostAccount]    Script Date: 06.03.2019 17:13:01 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[CostAccount](
	[csaId] [bigint] IDENTITY(1,1) NOT NULL,
	[csaCode] [nvarchar](5) NULL,
	[csaName] [nvarchar](50) NULL,
	[csacsaId] [bigint] NULL,
	[csaState] [smallint] NULL,
 CONSTRAINT [PK_CostAccount] PRIMARY KEY CLUSTERED 
(
	[csaId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[CostAccount]  WITH CHECK ADD  CONSTRAINT [FK_CostAccount_CostAccount] FOREIGN KEY([csacsaId])
REFERENCES [dbo].[CostAccount] ([csaId])
GO

ALTER TABLE [dbo].[CostAccount] CHECK CONSTRAINT [FK_CostAccount_CostAccount]
GO

/****** Object:  Table [dbo].[Periode]    Script Date: 06.03.2019 17:42:16 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Periode](
	[perId] [bigint] IDENTITY(1,1) NOT NULL,
	[perName] [nvarchar](80) NULL,
	[perYear] [int] NULL,
	[perMonth] [int] NULL,
	[percsaId] [bigint] NOT NULL,
	[perBookedExpense] [smallint] NULL,
	[perBookedProject] [smallint] NULL,
	[perState] [smallint] NULL,
 CONSTRAINT [PK_Periode] PRIMARY KEY CLUSTERED 
(
	[perId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Periode]  WITH CHECK ADD  CONSTRAINT [FK_Periode_CostAccount] FOREIGN KEY([percsaId])
REFERENCES [dbo].[CostAccount] ([csaId])
GO

ALTER TABLE [dbo].[Periode] CHECK CONSTRAINT [FK_Periode_CostAccount]
GO


/****** Object:  Table [dbo].[Project]    Script Date: 06.03.2019 17:43:55 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Project](
	[proId] [bigint] IDENTITY(1,1) NOT NULL,
	[proName] [nvarchar](50) NOT NULL,
	[proExtReference] [nvarchar](50) NULL,
	[proStartDate] [date] NOT NULL,
	[proEndDate] [date] NULL,
	[proHours] [int] NULL,
	[proIntensityPercent] [int] NULL,
	[procusId] [bigint] NOT NULL,
	[procsaId] [bigint] NULL,
	[proLastBill] [date] NULL,
	[proRate] [decimal](6, 2) NOT NULL,
	[proModel] [smallint] NULL,
	[provatId] [bigint] NULL,
	[proState] [smallint] NULL,
	[proproId] [bigint] NULL,
	[proDescription] [ntext] NULL,
	[proRemark] [ntext] NULL,
	[proProjectState] [smallint] NULL,
	[proHoursEffective]  AS ([dbo].[ufnGetGetReportingHours]([proId])),
 CONSTRAINT [PK_Project] PRIMARY KEY CLUSTERED 
(
	[proId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

ALTER TABLE [dbo].[Project]  WITH CHECK ADD  CONSTRAINT [FK_Project_CostAccount] FOREIGN KEY([procsaId])
REFERENCES [dbo].[CostAccount] ([csaId])
GO

ALTER TABLE [dbo].[Project] CHECK CONSTRAINT [FK_Project_CostAccount]
GO

ALTER TABLE [dbo].[Project]  WITH CHECK ADD  CONSTRAINT [FK_Project_Customer] FOREIGN KEY([procusId])
REFERENCES [dbo].[Customer] ([cusId])
GO

ALTER TABLE [dbo].[Project] CHECK CONSTRAINT [FK_Project_Customer]
GO

ALTER TABLE [dbo].[Project]  WITH CHECK ADD  CONSTRAINT [FK_Project_Project] FOREIGN KEY([proproId])
REFERENCES [dbo].[Project] ([proId])
GO

ALTER TABLE [dbo].[Project] CHECK CONSTRAINT [FK_Project_Project]
GO

ALTER TABLE [dbo].[Project]  WITH CHECK ADD  CONSTRAINT [FK_Project_Vat] FOREIGN KEY([provatId])
REFERENCES [dbo].[Vat] ([vatId])
GO

ALTER TABLE [dbo].[Project] CHECK CONSTRAINT [FK_Project_Vat]
GO


/****** Object:  Table [dbo].[Expense]    Script Date: 06.03.2019 17:44:34 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Expense](
	[expId] [bigint] IDENTITY(1,1) NOT NULL,
	[expperId] [bigint] NOT NULL,
	[expproId] [bigint] NOT NULL,
	[expAccount] [nvarchar](50) NULL,
	[expFlagCostAccount] [bit] NULL,
	[expFlagGeneric] [smallint] NULL,
	[expvatId] [bigint] NULL,
	[expDate] [date] NOT NULL,
	[expText] [nvarchar](128) NULL,
	[expUnit] [smallint] NULL,
	[expQuantity] [decimal](6, 2) NULL,
	[expAmount] [decimal](6, 2) NOT NULL,
	[expBooked] [date] NULL,
	[expState] [smallint] NULL,
 CONSTRAINT [PK_Expense] PRIMARY KEY CLUSTERED 
(
	[expId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Expense]  WITH CHECK ADD  CONSTRAINT [FK_Expense_Periode] FOREIGN KEY([expperId])
REFERENCES [dbo].[Periode] ([perId])
GO

ALTER TABLE [dbo].[Expense] CHECK CONSTRAINT [FK_Expense_Periode]
GO

ALTER TABLE [dbo].[Expense]  WITH CHECK ADD  CONSTRAINT [FK_Expense_Project] FOREIGN KEY([expproId])
REFERENCES [dbo].[Project] ([proId])
GO

ALTER TABLE [dbo].[Expense] CHECK CONSTRAINT [FK_Expense_Project]
GO

ALTER TABLE [dbo].[Expense]  WITH CHECK ADD  CONSTRAINT [FK_Expense_Vat] FOREIGN KEY([expvatId])
REFERENCES [dbo].[Vat] ([vatId])
GO

ALTER TABLE [dbo].[Expense] CHECK CONSTRAINT [FK_Expense_Vat]
GO


/****** Object:  Table [dbo].[ItemGroup]    Script Date: 06.03.2019 17:45:34 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[ItemGroup](
	[itgId] [bigint] IDENTITY(1,1) NOT NULL,
	[itgNumber] [int] NULL,
	[itgName] [nvarchar](40) NULL,
	[itgitgParent] [bigint] NULL,
	[itgState] [smallint] NULL,
 CONSTRAINT [PK__ItemGrou__BBB62F9A15502E78] PRIMARY KEY CLUSTERED 
(
	[itgId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[ItemGroup]  WITH CHECK ADD  CONSTRAINT [itg_Groups] FOREIGN KEY([itgitgParent])
REFERENCES [dbo].[ItemGroup] ([itgId])
GO

ALTER TABLE [dbo].[ItemGroup] CHECK CONSTRAINT [itg_Groups]
GO


/****** Object:  Table [dbo].[Item]    Script Date: 06.03.2019 17:45:54 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Item](
	[itmId] [bigint] IDENTITY(1,1) NOT NULL,
	[itmIdent] [nvarchar](40) NOT NULL,
	[itmName] [nvarchar](60) NULL,
	[itmvatId] [bigint] NULL,
	[itmPrice1] [numeric](10, 3) NULL,
	[itmPrice2] [numeric](10, 3) NULL,
	[itmUnit] [int] NULL,
	[itmitgId] [bigint] NULL,
	[itmState] [smallint] NULL,
 CONSTRAINT [PK__Item__8EE9F2B3239E4DCF] PRIMARY KEY CLUSTERED 
(
	[itmId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Item]  WITH CHECK ADD  CONSTRAINT [itm_itg_fk] FOREIGN KEY([itmitgId])
REFERENCES [dbo].[ItemGroup] ([itgId])
GO

ALTER TABLE [dbo].[Item] CHECK CONSTRAINT [itm_itg_fk]
GO

ALTER TABLE [dbo].[Item]  WITH CHECK ADD  CONSTRAINT [itm_vat] FOREIGN KEY([itmvatId])
REFERENCES [dbo].[Vat] ([vatId])
GO

ALTER TABLE [dbo].[Item] CHECK CONSTRAINT [itm_vat]
GO


/****** Object:  Table [dbo].[Language]    Script Date: 06.03.2019 17:46:40 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Language](
	[lngId] [bigint] IDENTITY(0,1) NOT NULL,
	[lngCode] [int] NOT NULL,
	[lngName] [nvarchar](40) NOT NULL,
	[lngIsocode] [nvarchar](4) NULL,
	[lngKeyboard] [nvarchar](40) NULL,
	[lngDefault] [bit] NULL,
	[lngState] [smallint] NULL,
 CONSTRAINT [PK_Language] PRIMARY KEY NONCLUSTERED 
(
	[lngId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO


/****** Object:  Table [dbo].[Order]    Script Date: 06.03.2019 17:47:08 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Order](
	[ordId] [bigint] IDENTITY(1,1) NOT NULL,
	[ordNumber] [int] NOT NULL,
	[ordState] [smallint] NULL,
	[ordCreated] [datetime] NULL,
	[ordCreatedBy] [nvarchar](20) NULL,
	[ordOrderDate] [datetime] NULL,
	[ordBillDate] [datetime] NULL,
	[ordcusId] [bigint] NOT NULL,
	[ordAmountBrut] [numeric](10, 3) NULL,
	[ordAmountNet] [numeric](10, 3) NULL,
	[ordPayDate] [datetime] NULL,
	[ordText] [nvarchar](256) NULL,
	[ordDueDate] [datetime] NULL,
	[ordpacId] [bigint] NOT NULL,
	[ordBookedOn] [datetime] NULL,
	[ordproId] [bigint] NULL,
 CONSTRAINT [PK__Order__215DC1B829572725] PRIMARY KEY CLUSTERED 
(
	[ordId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Order] ADD  CONSTRAINT [DF_Order_ordpcaId]  DEFAULT ((1)) FOR [ordpacId]
GO

ALTER TABLE [dbo].[Order]  WITH CHECK ADD  CONSTRAINT [FK_Order_PaymentCondition] FOREIGN KEY([ordpacId])
REFERENCES [dbo].[PaymentCondition] ([pacId])
GO

ALTER TABLE [dbo].[Order] CHECK CONSTRAINT [FK_Order_PaymentCondition]
GO

ALTER TABLE [dbo].[Order]  WITH CHECK ADD  CONSTRAINT [FK_Order_Project] FOREIGN KEY([ordproId])
REFERENCES [dbo].[Project] ([proId])
GO

ALTER TABLE [dbo].[Order] CHECK CONSTRAINT [FK_Order_Project]
GO

ALTER TABLE [dbo].[Order]  WITH CHECK ADD  CONSTRAINT [ord_fk_cus] FOREIGN KEY([ordcusId])
REFERENCES [dbo].[Customer] ([cusId])
GO

ALTER TABLE [dbo].[Order] CHECK CONSTRAINT [ord_fk_cus]
GO


/****** Object:  Table [dbo].[OrderLine]    Script Date: 06.03.2019 17:47:23 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[OrderLine](
	[odlId] [bigint] IDENTITY(1,1) NOT NULL,
	[odlNumber] [int] NOT NULL,
	[odlordId] [bigint] NULL,
	[odlitmId] [bigint] NOT NULL,
	[odlQuantity] [numeric](10, 4) NOT NULL,
	[odlPrice] [numeric](10, 3) NULL,
	[odlvatId] [bigint] NOT NULL,
	[odlAmountBrut] [numeric](10, 3) NULL,
	[odlAmountNet] [numeric](10, 3) NULL,
	[odlText] [nvarchar](80) NULL,
	[odlVatAmount] [numeric](10, 3) NULL,
	[odlDiscount] [numeric](10, 3) NULL,
	[odlcsaId] [bigint] NOT NULL,
	[odlState] [smallint] NULL,
 CONSTRAINT [PK__OrderLin__FE6AC41D2F10007B] PRIMARY KEY CLUSTERED 
(
	[odlId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[OrderLine]  WITH CHECK ADD  CONSTRAINT [FK_OrderLine_CostAccount] FOREIGN KEY([odlcsaId])
REFERENCES [dbo].[CostAccount] ([csaId])
GO

ALTER TABLE [dbo].[OrderLine] CHECK CONSTRAINT [FK_OrderLine_CostAccount]
GO

ALTER TABLE [dbo].[OrderLine]  WITH CHECK ADD  CONSTRAINT [odl_fk_item] FOREIGN KEY([odlitmId])
REFERENCES [dbo].[Item] ([itmId])
GO

ALTER TABLE [dbo].[OrderLine] CHECK CONSTRAINT [odl_fk_item]
GO

ALTER TABLE [dbo].[OrderLine]  WITH CHECK ADD  CONSTRAINT [odl_fk_order] FOREIGN KEY([odlordId])
REFERENCES [dbo].[Order] ([ordId])
GO

ALTER TABLE [dbo].[OrderLine] CHECK CONSTRAINT [odl_fk_order]
GO

ALTER TABLE [dbo].[OrderLine]  WITH CHECK ADD  CONSTRAINT [odl_fk_vat] FOREIGN KEY([odlvatId])
REFERENCES [dbo].[Vat] ([vatId])
GO

ALTER TABLE [dbo].[OrderLine] CHECK CONSTRAINT [odl_fk_vat]
GO


/****** Object:  Table [dbo].[ProjectLine]    Script Date: 06.03.2019 17:48:00 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[ProjectLine](
	[prlId] [bigint] IDENTITY(1,1) NOT NULL,
	[prlperId] [bigint] NOT NULL,
	[prlproId] [bigint] NOT NULL,
	[prlReportDate] [datetime] NOT NULL,
	[prlHours] [decimal](6, 2) NULL,
	[prlText] [nvarchar](384) NULL,
	[prlitmId] [bigint] NULL,
	[prlWorkType] [smallint] NULL,
	[prlRate] [decimal](6, 2) NULL,
	[prlState] [smallint] NULL,
 CONSTRAINT [PK_ProjectLine] PRIMARY KEY CLUSTERED 
(
	[prlId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[ProjectLine]  WITH CHECK ADD  CONSTRAINT [FK_ProjectLine_Item] FOREIGN KEY([prlperId])
REFERENCES [dbo].[Periode] ([perId])
GO

ALTER TABLE [dbo].[ProjectLine] CHECK CONSTRAINT [FK_ProjectLine_Item]
GO

ALTER TABLE [dbo].[ProjectLine]  WITH CHECK ADD  CONSTRAINT [FK_ProjectLine_Project] FOREIGN KEY([prlproId])
REFERENCES [dbo].[Project] ([proId])
GO

ALTER TABLE [dbo].[ProjectLine] CHECK CONSTRAINT [FK_ProjectLine_Project]
GO



/****** Object:  Table [dbo].[ResPlanning]    Script Date: 06.03.2019 17:48:36 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[ResPlanning](
	[rspId] [bigint] IDENTITY(1,1) NOT NULL,
	[rspPlandate] [date] NOT NULL,
	[rspMode] [smallint] NOT NULL,
	[rspproId] [bigint] NULL,
	[rspcsaId] [bigint] NOT NULL,
	[rspPercent] [int] NOT NULL,
	[rspHours] [numeric](10, 2) NOT NULL,
	[rspState] [smallint] NOT NULL,
 CONSTRAINT [PK_ResPlanning] PRIMARY KEY CLUSTERED 
(
	[rspId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[ResPlanning] ADD  CONSTRAINT [DF_ResPlanning_rspHours]  DEFAULT ((0)) FOR [rspHours]
GO

ALTER TABLE [dbo].[ResPlanning]  WITH CHECK ADD  CONSTRAINT [FK_ResPlanning_CostAccount] FOREIGN KEY([rspcsaId])
REFERENCES [dbo].[CostAccount] ([csaId])
GO

ALTER TABLE [dbo].[ResPlanning] CHECK CONSTRAINT [FK_ResPlanning_CostAccount]
GO

ALTER TABLE [dbo].[ResPlanning]  WITH CHECK ADD  CONSTRAINT [FK_ResPlanning_Project] FOREIGN KEY([rspproId])
REFERENCES [dbo].[Project] ([proId])
GO

ALTER TABLE [dbo].[ResPlanning] CHECK CONSTRAINT [FK_ResPlanning_Project]
GO


/****** Object:  Table [dbo].[DatabaseVersion]    Script Date: 06.03.2019 17:49:33 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[DatabaseVersion](
	[dbvId] [bigint] IDENTITY(0,1) NOT NULL,
	[dbvMajor] [int] NULL,
	[dbvMinor] [int] NULL,
	[dbvMicro] [nvarchar](40) NULL,
	[dbvReleased] [datetime] NULL,
	[dbvDescription] [ntext] NULL,
	[dbvState] [smallint] NULL,
 CONSTRAINT [PK_DatabaseVersion] PRIMARY KEY CLUSTERED 
(
	[dbvId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO


/****** Object:  Table [dbo].[RowObject]    Script Date: 06.03.2019 17:49:49 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[RowObject](
	[objId] [bigint] IDENTITY(0,1) NOT NULL,
	[objentId] [bigint] NOT NULL,
	[objRowId] [bigint] NOT NULL,
	[objChngcnt] [bigint] NULL,
	[objState] [smallint] NULL,
	[objAdded] [datetime] NULL,
	[objAddedBy] [nvarchar](30) NULL,
	[objChanged] [datetime] NULL,
	[objChangedBy] [nvarchar](30) NULL,
	[objDeleted] [datetime] NULL,
	[objDeletedBy] [nvarchar](30) NULL,
	[objdbvId] [bigint] NULL,
 CONSTRAINT [PK_RowObject] PRIMARY KEY NONCLUSTERED 
(
	[objId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[RowObject]  WITH CHECK ADD  CONSTRAINT [DatabaseVersion_RowObject] FOREIGN KEY([objdbvId])
REFERENCES [dbo].[DatabaseVersion] ([dbvId])
GO

ALTER TABLE [dbo].[RowObject] CHECK CONSTRAINT [DatabaseVersion_RowObject]
GO

ALTER TABLE [dbo].[RowObject]  WITH CHECK ADD  CONSTRAINT [Entity_RowObject] FOREIGN KEY([objentId])
REFERENCES [dbo].[Entity] ([entId])
GO

ALTER TABLE [dbo].[RowObject] CHECK CONSTRAINT [Entity_RowObject]
GO


/****** Object:  Table [dbo].[RowImage]    Script Date: 06.03.2019 17:50:08 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[RowImage](
	[rimId] [bigint] IDENTITY(0,1) NOT NULL,
	[rimobjId] [bigint] NOT NULL,
	[rimName] [nvarchar](128) NULL,
	[rimIcon] [image] NULL,
	[rimImage] [image] NULL,
	[rimState] [smallint] NULL,
	[rimMimetype] [nvarchar](60) NULL,
	[rimNumber] [int] NOT NULL,
	[rimType] [smallint] NOT NULL,
	[rimSize] [nvarchar](10) NULL,
 CONSTRAINT [PK_RowImage] PRIMARY KEY CLUSTERED 
(
	[rimId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

ALTER TABLE [dbo].[RowImage] ADD  CONSTRAINT [DF_RowImage_rimNumber]  DEFAULT ((1)) FOR [rimNumber]
GO

ALTER TABLE [dbo].[RowImage] ADD  CONSTRAINT [DF_RowImage_rimType]  DEFAULT ((0)) FOR [rimType]
GO

ALTER TABLE [dbo].[RowImage]  WITH CHECK ADD  CONSTRAINT [RowObject_RowImage] FOREIGN KEY([rimobjId])
REFERENCES [dbo].[RowObject] ([objId])
GO

ALTER TABLE [dbo].[RowImage] CHECK CONSTRAINT [RowObject_RowImage]
GO


/****** Object:  Table [dbo].[RowLabel]    Script Date: 06.03.2019 17:50:23 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[RowLabel](
	[lblId] [bigint] IDENTITY(0,1) NOT NULL,
	[lblobjId] [bigint] NOT NULL,
	[lbllngId] [bigint] NOT NULL,
	[lblLabelShort] [nvarchar](10) NULL,
	[lblLabelLong] [nvarchar](40) NULL,
	[lblState] [smallint] NULL,
 CONSTRAINT [PK_RowLabel] PRIMARY KEY CLUSTERED 
(
	[lblId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[RowLabel]  WITH CHECK ADD  CONSTRAINT [Language_RowLabel] FOREIGN KEY([lbllngId])
REFERENCES [dbo].[Language] ([lngId])
GO

ALTER TABLE [dbo].[RowLabel] CHECK CONSTRAINT [Language_RowLabel]
GO

ALTER TABLE [dbo].[RowLabel]  WITH CHECK ADD  CONSTRAINT [RowObject_RowLabel] FOREIGN KEY([lblobjId])
REFERENCES [dbo].[RowObject] ([objId])
GO

ALTER TABLE [dbo].[RowLabel] CHECK CONSTRAINT [RowObject_RowLabel]
GO


/****** Object:  Table [dbo].[RowParameter]    Script Date: 06.03.2019 17:50:37 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[RowParameter](
	[prmId] [bigint] IDENTITY(0,1) NOT NULL,
	[prmobjId] [bigint] NOT NULL,
	[prmGroup] [nvarchar](40) NULL,
	[prmSubGroup] [nvarchar](40) NULL,
	[prmKey] [nvarchar](20) NULL,
	[prmValue] [nvarchar](128) NULL,
	[prmValueType] [smallint] NULL,
	[prmState] [smallint] NULL,
	[prmParamType] [smallint] NULL,
	[prmLookupTable] [nvarchar](40) NULL,
	[prmVisible] [bit] NULL,
 CONSTRAINT [PK_RowParameter] PRIMARY KEY NONCLUSTERED 
(
	[prmId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[RowParameter]  WITH CHECK ADD  CONSTRAINT [RowObject_RowParameter] FOREIGN KEY([prmobjId])
REFERENCES [dbo].[RowObject] ([objId])
GO

ALTER TABLE [dbo].[RowParameter] CHECK CONSTRAINT [RowObject_RowParameter]
GO


/****** Object:  Table [dbo].[RowRelation]    Script Date: 06.03.2019 17:50:49 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[RowRelation](
	[relId] [bigint] IDENTITY(0,1) NOT NULL,
	[relName] [nvarchar](40) NOT NULL,
	[relOrder] [int] NULL,
	[relDescription] [nvarchar](80) NULL,
	[relobjId_Source] [bigint] NOT NULL,
	[relobjId_Target] [bigint] NOT NULL,
	[relState] [smallint] NULL,
 CONSTRAINT [PK_RowRelation] PRIMARY KEY CLUSTERED 
(
	[relId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[RowRelation]  WITH CHECK ADD  CONSTRAINT [RowObject_RowRelation_Source] FOREIGN KEY([relobjId_Source])
REFERENCES [dbo].[RowObject] ([objId])
GO

ALTER TABLE [dbo].[RowRelation] CHECK CONSTRAINT [RowObject_RowRelation_Source]
GO

ALTER TABLE [dbo].[RowRelation]  WITH CHECK ADD  CONSTRAINT [RowObject_RowRelation_Target] FOREIGN KEY([relobjId_Target])
REFERENCES [dbo].[RowObject] ([objId])
GO

ALTER TABLE [dbo].[RowRelation] CHECK CONSTRAINT [RowObject_RowRelation_Target]
GO


/****** Object:  Table [dbo].[RowSecurity]    Script Date: 06.03.2019 17:51:03 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[RowSecurity](
	[secId] [bigint] IDENTITY(0,1) NOT NULL,
	[secobjId] [bigint] NOT NULL,
	[secType] [int] NOT NULL,
	[secValidfrom] [datetime] NULL,
	[secValidto] [nvarchar](40) NULL,
	[secState] [smallint] NOT NULL,
	[secPermissionKey] [nvarchar](40) NULL,
 CONSTRAINT [PK_RowSecurity] PRIMARY KEY CLUSTERED 
(
	[secId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[RowSecurity] ADD  CONSTRAINT [DEF_RowSecurity_secType]  DEFAULT ((0)) FOR [secType]
GO

ALTER TABLE [dbo].[RowSecurity] ADD  CONSTRAINT [DEF_RowSecurity_secState]  DEFAULT ((0)) FOR [secState]
GO

ALTER TABLE [dbo].[RowSecurity]  WITH CHECK ADD  CONSTRAINT [RowObject_RowSecurity] FOREIGN KEY([secobjId])
REFERENCES [dbo].[RowObject] ([objId])
GO

ALTER TABLE [dbo].[RowSecurity] CHECK CONSTRAINT [RowObject_RowSecurity]
GO


/****** Object:  Table [dbo].[RowText]    Script Date: 06.03.2019 17:51:17 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[RowText](
	[txtId] [bigint] IDENTITY(0,1) NOT NULL,
	[txtobjId] [bigint] NOT NULL,
	[txtlngId] [bigint] NOT NULL,
	[txtNumber] [int] NULL,
	[txtFreetext] [ntext] NULL,
	[txtState] [smallint] NULL,
 CONSTRAINT [PK_RowText] PRIMARY KEY NONCLUSTERED 
(
	[txtId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

ALTER TABLE [dbo].[RowText]  WITH CHECK ADD  CONSTRAINT [Language_RowText] FOREIGN KEY([txtlngId])
REFERENCES [dbo].[Language] ([lngId])
GO

ALTER TABLE [dbo].[RowText] CHECK CONSTRAINT [Language_RowText]
GO

ALTER TABLE [dbo].[RowText]  WITH CHECK ADD  CONSTRAINT [RowObject_RowText] FOREIGN KEY([txtobjId])
REFERENCES [dbo].[RowObject] ([objId])
GO

ALTER TABLE [dbo].[RowText] CHECK CONSTRAINT [RowObject_RowText]
GO


/****** Object:  Table [dbo].[StateCode]    Script Date: 06.03.2019 17:51:32 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[StateCode](
	[stcId] [bigint] IDENTITY(0,1) NOT NULL,
	[stcentId] [bigint] NOT NULL,
	[stcFieldname] [nvarchar](40) NULL,
	[stcCode] [int] NULL,
	[stcCodeName] [nvarchar](40) NULL,
	[stcState] [smallint] NULL,
 CONSTRAINT [PK_StateCode] PRIMARY KEY CLUSTERED 
(
	[stcId] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[StateCode]  WITH CHECK ADD  CONSTRAINT [Entity_StateCode] FOREIGN KEY([stcentId])
REFERENCES [dbo].[Entity] ([entId])
GO

ALTER TABLE [dbo].[StateCode] CHECK CONSTRAINT [Entity_StateCode]
GO





