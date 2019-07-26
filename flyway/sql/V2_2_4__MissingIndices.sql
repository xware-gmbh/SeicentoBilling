/****** Object:  Index [IX_Project_APK]    Script Date: 19.07.2019 11:30:01 ******/

SET ANSI_PADDING ON
GO

IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'IX_Project_APK')
begin
	CREATE UNIQUE NONCLUSTERED INDEX [IX_Project_APK] ON [dbo].[Project]
	(
		[proName] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]
end
GO

/****** Object:  Index [IDX_City_1]    Script Date: 19.07.2019 15:31:11 ******/
IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'IDX_City_1')
begin
	CREATE UNIQUE NONCLUSTERED INDEX [IDX_City_1] ON [dbo].[City]
	(
		[ctyZIP] ASC,
		[ctyCountry] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]
	
	/****** Object:  Index [IX_City_Zip]    Script Date: 19.07.2019 15:32:55 ******/
	CREATE UNIQUE NONCLUSTERED INDEX [IX_City_Zip] ON [dbo].[City]
	(
		[ctyZIP] ASC,
		[ctyCountry] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]
end 
GO

/****** Object:  Index [IX_CostAccount_APK]    Script Date: 19.07.2019 15:34:42 ******/
IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'IX_CostAccount_APK')
begin
	CREATE UNIQUE NONCLUSTERED INDEX [IX_CostAccount_APK] ON [dbo].[CostAccount]
	(
		[csaCode] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]
end
GO

/****** Object:  Index [IX_Customer_APK]    Script Date: 19.07.2019 15:37:12 ******/
IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'IX_Customer_APK')
begin
	CREATE UNIQUE NONCLUSTERED INDEX [IX_Customer_APK] ON [dbo].[Customer]
	(
		[cusNumber] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]
end
GO


/****** Object:  Index [IDX_Entity_1]    Script Date: 19.07.2019 15:38:28 ******/
IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'IDX_Entity_1')
begin
	CREATE UNIQUE CLUSTERED INDEX [IDX_Entity_1] ON [dbo].[Entity]
	(
		[entName] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]
end
GO

/****** Object:  Index [IX_Item_APK]    Script Date: 19.07.2019 15:39:39 ******/
IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'IX_Item_APK')
begin
	CREATE UNIQUE NONCLUSTERED INDEX [IX_Item_APK] ON [dbo].[Item]
	(
		[itmIdent] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]

	CREATE UNIQUE NONCLUSTERED INDEX [IX_ItemGroup_APK] ON [dbo].[ItemGroup]
	(
		[itgNumber] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]

end
GO

/****** Object:  Index [IX_Order]    Script Date: 19.07.2019 15:41:06 ******/
IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'IX_Order')
begin
	CREATE UNIQUE NONCLUSTERED INDEX [IX_Order] ON [dbo].[Order]
	(
		[ordNumber] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]
end
GO

/****** Object:  Index [IX_Periode_APK]    Script Date: 19.07.2019 15:42:47 ******/
IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'IX_Periode_APK')
begin
	/****** Object:  Index [IX_Periode_APK]    Script Date: 19.07.2019 15:42:47 ******/
	CREATE UNIQUE NONCLUSTERED INDEX [IX_Periode_APK] ON [dbo].[Periode]
	(
		[perYear] DESC,
		[perMonth] DESC,
		[percsaId] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]

	/****** Object:  Index [IDX_RowObject_1]    Script Date: 19.07.2019 15:43:42 ******/
	CREATE UNIQUE CLUSTERED INDEX [IDX_RowObject_1] ON [dbo].[RowObject]
	(
		[objentId] ASC,
		[objRowId] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]
	
	/****** Object:  Index [IDX_RowParameter_1]    Script Date: 19.07.2019 15:44:09 ******/
	CREATE UNIQUE CLUSTERED INDEX [IDX_RowParameter_1] ON [dbo].[RowParameter]
	(
		[prmobjId] ASC,
		[prmGroup] ASC,
		[prmSubGroup] ASC,
		[prmKey] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]
	
	/****** Object:  Index [IDX_RowText_1]    Script Date: 19.07.2019 15:44:40 ******/
	CREATE UNIQUE CLUSTERED INDEX [IDX_RowText_1] ON [dbo].[RowText]
	(
		[txtobjId] ASC,
		[txtlngId] ASC,
		[txtNumber] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]
	
	/****** Object:  Index [IX_Vat_APK]    Script Date: 19.07.2019 15:45:17 ******/
	CREATE UNIQUE NONCLUSTERED INDEX [IX_Vat_APK] ON [dbo].[Vat]
	(
		[vatSign] ASC
	)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) ON [PRIMARY]
end
GO



