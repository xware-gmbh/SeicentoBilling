
    create table dbo."Order" (
       ordId  bigserial not null,
        ordAmountBrut numeric,
        ordAmountNet numeric,
        ordBillDate datetime,
        ordBookedOn datetime,
        ordCreated datetime,
        ordCreatedBy nvarchar,
        ordDueDate datetime,
        ordNumber int not null,
        ordOrderDate datetime,
        ordPayDate datetime,
        ordState smallint,
        ordText nvarchar,
        ordcusId bigint not null,
        ordpacId bigint not null,
        ordproId bigint,
        primary key (ordId)
    );

    create table dbo.Activity (
       actId  bigserial not null,
        actDate datetime not null,
        actFollowingUpDate datetime not null,
        actLink nvarchar,
        actState smallint,
        actText ntext,
        actType smallint,
        actcsaId bigint,
        actcusId bigint,
        primary key (actId)
    );

    create table dbo.Address (
       adrId  bigserial not null,
        adrAddOn nvarchar,
        adrCity nvarchar,
        adrCountry nvarchar,
        adrIndex smallint,
        adrLine0 nvarchar,
        adrLine1 nvarchar,
        adrName nvarchar,
        adrRegion nvarchar,
        adrRemark nvarchar,
        adrSalutation smallint,
        adrState smallint,
        adrType smallint,
        adrValidFrom datetime not null,
        adrZip nvarchar,
        adrcusId bigint,
        primary key (adrId)
    );

    create table dbo.AppUser (
       usrId  bigserial not null,
        "password" varbinary not null,
        username nvarchar not null,
        usrCountry nvarchar,
        usrFullName nvarchar,
        usrLanguage nvarchar,
        usrRoles nvarchar,
        usrState smallint,
        usrThemeDesktop smallint,
        usrThemeMobile smallint,
        usrTimeZone nvarchar,
        usrValidFrom datetime,
        usrValidTo datetime,
        usrcsaId bigint,
        usrcusId bigint,
        primary key (usrId)
    );

    create table dbo.Bank (
       bnkId  bigserial not null,
        bnkAccount nvarchar,
        bnkAddress nvarchar,
        bnkCustomernbr bigint,
        bnkEsrTn nchar,
        bnkIban nvarchar,
        bnkName nvarchar,
        bnkState smallint,
        bnkctyId bigint,
        primary key (bnkId)
    );

    create table dbo.City (
       ctyId  bigserial not null,
        ctyCountry nvarchar,
        ctyGeoCoordinates nvarchar,
        ctyName nvarchar not null,
        ctyRegion nvarchar,
        ctyState smallint,
        ctyZIP int,
        primary key (ctyId)
    );

    create table dbo.Communication (
       comId  bigserial not null,
        comDescription nvarchar,
        comNumber int,
        comState smallint,
        comType smallint,
        comValue nvarchar,
        combrnId bigint not null,
        primary key (comId)
    );

    create table dbo.Company (
       cmpId  bigserial not null,
        cmpAbaActive bit,
        cmpAbaEndpointCre nvarchar,
        cmpAbaEndpointCreDoc nvarchar,
        cmpAbaEndpointCus nvarchar,
        cmpAbaEndpointDoc nvarchar,
        cmpAbaEndpointPay nvarchar,
        cmpAbaMandator int,
        cmpAbaMaxDays int,
        cmpAbaUser nvarchar,
        cmpAddress nvarchar,
        cmpBookingYear int,
        cmpBusiness nvarchar,
        cmpComm1 nvarchar,
        cmpCurrency nvarchar,
        cmpJasperUri nchar,
        cmpLastCustomerNbr int,
        cmpLastItemNbr int,
        cmpLastOrderNbr int,
        cmpLogo image,
        cmpMail nvarchar,
        cmpName nvarchar,
        cmpPhone nvarchar,
        cmpPlace nvarchar,
        cmpReportPwd nvarchar,
        cmpReportUsr nvarchar,
        cmpState int4,
        cmpUid nvarchar,
        cmpVatcode nvarchar,
        cmpZip int,
        primary key (cmpId)
    );

    create table dbo.ContactRelation (
       corId  bigserial not null,
        corTypeOne int4,
        corTypeTwo int4,
        corcusIdTypeOne bigint,
        corcusIdTypeTwo bigint,
        primary key (corId)
    );

    create table dbo.Conversion (
       cnvId  bigserial not null,
        cnvDataType smallint,
        cnvGroup nvarchar,
        cnvRemark nvarchar,
        cnvState smallint,
        cnvSubGroup nvarchar,
        cnvValueIn nvarchar,
        cnvValueOut nvarchar,
        primary key (cnvId)
    );

    create table dbo.CostAccount (
       csaId  bigserial not null,
        csaCode nvarchar,
        csaExtRef nvarchar,
        csaName nvarchar,
        csaState smallint,
        csacsaId bigint,
        primary key (csaId)
    );

    create table dbo.Customer (
       cusId  bigserial not null,
        cusAccountManager varchar(255),
        cusAccountType smallint,
        cusAddress nvarchar,
        cusBillingReport int4,
        cusBillingTarget int4,
        cusBirthdate date,
        cusCompany nvarchar,
        cusExtRef1 nvarchar,
        cusExtRef2 nvarchar,
        cusFirstName nvarchar,
        cusInfo ntext,
        cusLastBill date,
        cusName nvarchar not null,
        cusNumber int not null,
        cusSalutation int4,
        cusSinglePdf bit,
        cusState smallint,
        cusctyId bigint,
        cuspacId bigint not null,
        primary key (cusId)
    );

    create table dbo.CustomerLink (
       cnkId  bigserial not null,
        cnkDepartment smallint,
        cnkIndex smallint,
        cnkLink nvarchar,
        cnkRemark nvarchar,
        cnkState smallint,
        cnkType smallint,
        cnkValidFrom datetime not null,
        cnkcusId bigint,
        primary key (cnkId)
    );

    create table dbo.Entity (
       entId  bigserial not null,
        entAbbreviation nvarchar,
        entAuditHistory smallint,
        entDataclass nvarchar,
        entExport2sdf bit,
        entHasrowobject bit,
        entName nvarchar,
        entReadonly bit,
        entSdfOrdinal smallint,
        entState smallint,
        entType int,
        primary key (entId)
    );

    create table dbo.Expense (
       expId  bigserial not null,
        expAccount nvarchar,
        expAmount decimal not null,
        expBooked date,
        expDate date not null,
        expFlagCostAccount bit,
        expFlagGeneric smallint,
        expQuantity decimal,
        expState smallint,
        expText nvarchar,
        expUnit smallint,
        expperId bigint not null,
        expproId bigint not null,
        expvatId bigint,
        primary key (expId)
    );

    create table dbo.ExpenseTemplate (
       extId  bigserial not null,
        extAccount nvarchar,
        extAmount decimal not null,
        extFlagCostAccount bit,
        extFlagGeneric smallint,
        extKeyNumber integer not null,
        extQuantity decimal,
        extState smallint,
        extText nvarchar,
        extUnit smallint,
        extcsaId bigint not null,
        extproId bigint not null,
        extvatId bigint,
        primary key (extId)
    );

    create table dbo.Item (
       itmId  bigserial not null,
        itmAccount numeric,
        itmIdent nvarchar not null,
        itmName nvarchar,
        itmPrice1 numeric,
        itmPrice2 numeric,
        itmState smallint,
        itmUnit int,
        itmitgId bigint,
        itmvatId bigint,
        primary key (itmId)
    );

    create table dbo.ItemGroup (
       itgId  bigserial not null,
        itgName nvarchar,
        itgNumber int,
        itgState smallint,
        itgitgParent bigint,
        primary key (itgId)
    );

    create table dbo.LabelAssignment (
       claId bigint identity not null,
        claIndex smallint,
        clacusId bigint not null,
        clacldId  bigserial not null,
        primary key (clacldId, clacusId)
    );

    create table dbo.LabelDefinition (
       cldId  bigserial not null,
        cldState smallint,
        cldText nvarchar,
        cldType smallint,
        primary key (cldId)
    );

    create table dbo.Language (
       lngId  bigserial not null,
        lngCode int not null,
        lngDefault bit,
        lngIsocode nvarchar,
        lngKeyboard nvarchar,
        lngName nvarchar not null,
        lngState smallint,
        primary key (lngId)
    );

    create table dbo.OrderLine (
       odlId  bigserial not null,
        odlAmountBrut numeric,
        odlAmountNet numeric,
        odlDiscount numeric,
        odlNumber int not null,
        odlPrice numeric,
        odlQuantity numeric not null,
        odlState smallint,
        odlText nvarchar,
        odlVatAmount numeric,
        odlcsaId bigint not null,
        odlitmId bigint not null,
        odlordId bigint,
        odlvatId bigint not null,
        primary key (odlId)
    );

    create table dbo.PaymentCondition (
       pacId  bigserial not null,
        pacCode nvarchar,
        pacExtRef1 varchar(20),
        pacName nvarchar,
        pacNbrOfDays int not null,
        pacState smallint,
        primary key (pacId)
    );

    create table dbo.Periode (
       perId  bigserial not null,
        perBookedExpense smallint,
        perBookedProject smallint,
        perMonth int,
        perName nvarchar,
        perSignOffExpense boolean,
        perState smallint,
        perYear int,
        percsaId bigint not null,
        primary key (perId)
    );

    create table dbo.Project (
       proId  bigserial not null,
        proInternal bit,
        proContact nvarchar,
        proDescription ntext,
        proEndDate date,
        proExtReference nvarchar,
        proHours int,
        proHoursEffective decimal not null,
        proIntensityPercent int,
        proLastBill date,
        proModel smallint,
        proName nvarchar not null,
        proProjectState smallint,
        proRate decimal not null,
        proRemark ntext,
        proStartDate date not null,
        proState smallint,
        proadrId bigint,
        procsaId bigint,
        procusId bigint not null,
        proproId bigint,
        provatId bigint,
        primary key (proId)
    );

    create table dbo.ProjectLine (
       prlId  bigserial not null,
        prlHours decimal,
        prlRate decimal,
        prlReportDate datetime not null,
        prlState smallint,
        prlText nvarchar,
        prlTimeFrom timestamp,
        prlTimeTo timestamp,
        prlWorkType smallint,
        prlitmId bigint,
        prlperId bigint not null,
        prlproId bigint not null,
        primary key (prlId)
    );

    create table dbo.ProjectLineTemplate (
       prtId  bigserial not null,
        prtHours decimal,
        prtKeyNumber int4,
        prtRate decimal,
        prtState smallint,
        prtText nvarchar,
        prtWorkType smallint,
        prtcsaId bigint not null,
        prtproId bigint not null,
        primary key (prtId)
    );

    create table dbo.ResPlanning (
       rspId  bigserial not null,
        rspHours numeric not null,
        rspMode smallint not null,
        rspPercent int not null,
        rspPlandate date not null,
        rspState smallint not null,
        rspcsaId bigint not null,
        rspproId bigint,
        primary key (rspId)
    );

    create table dbo.RowImage (
       rimId  bigserial not null,
        rimIcon image,
        rimImage image,
        rimMimetype nvarchar,
        rimName nvarchar,
        rimNumber int not null,
        rimSize nvarchar,
        rimState smallint,
        rimType smallint not null,
        rimobjId bigint not null,
        primary key (rimId)
    );

    create table dbo.RowLabel (
       lblId  bigserial not null,
        lblLabelLong nvarchar,
        lblLabelShort nvarchar,
        lblState smallint,
        lbllngId bigint not null,
        lblobjId bigint not null,
        primary key (lblId)
    );

    create table dbo.RowObject (
       objId  bigserial not null,
        objAdded datetime,
        objAddedBy nvarchar,
        objChanged datetime,
        objChangedBy nvarchar,
        objChngcnt bigint,
        objDeleted datetime,
        objDeletedBy nvarchar,
        objRowId bigint not null,
        objState smallint,
        objentId bigint not null,
        primary key (objId)
    );

    create table dbo.RowParameter (
       prmId  bigserial not null,
        prmGroup nvarchar,
        prmKey nvarchar,
        prmLookupTable nvarchar,
        prmParamType smallint,
        prmState smallint,
        prmSubGroup nvarchar,
        prmValue nvarchar,
        prmValueType smallint,
        prmVisible bit,
        prmobjId bigint not null,
        primary key (prmId)
    );

    create table dbo.RowRelation (
       relId  bigserial not null,
        relDescription nvarchar,
        relName nvarchar not null,
        relOrder int,
        relState smallint,
        relobjId_Source bigint not null,
        relobjId_Target bigint not null,
        primary key (relId)
    );

    create table dbo.RowText (
       txtId  bigserial not null,
        txtFreetext ntext,
        txtNumber int,
        txtState smallint,
        txtlngId bigint not null,
        txtobjId bigint not null,
        primary key (txtId)
    );

    create table dbo.StateCode (
       stcId  bigserial not null,
        stcCode int,
        stcCodeName nvarchar,
        stcFieldname nvarchar,
        stcState smallint,
        stcentId bigint not null,
        primary key (stcId)
    );

    create table dbo.Vat (
       vatId  bigserial not null,
        vatExtRef nvarchar,
        vatExtRef1 varchar(20),
        vatInclude bit,
        vatName nvarchar,
        vatRate numeric,
        vatSign nvarchar,
        vatState smallint,
        primary key (vatId)
    );

    create table dbo.VatLine (
       vanId  bigserial not null,
        vanRate numeric,
        vanRemark nvarchar,
        vanState smallint,
        vanValidFrom date,
        vanvatId bigint not null,
        primary key (vanId)
    );

    alter table if exists dbo."Order" 
       add constraint UK_tre2yben7y3bqxshh3y48661b unique (ordNumber);

    alter table if exists dbo.AppUser 
       add constraint UK_atqgqm46rh7b0lrgl80ryd5tp unique (username);

    alter table if exists dbo.City 
       add constraint UK4r44x1d04o5km4o9ykxj1b56v unique (ctyZIP, ctyCountry);

    alter table if exists dbo.Communication 
       add constraint UKoqwh50kungpskjct9si1d4j9m unique (comNumber, combrnId);

    alter table if exists dbo.CostAccount 
       add constraint UK_6182gtku5tmqan8531jfkwkbd unique (csaCode);

    alter table if exists dbo.Customer 
       add constraint UK_po43tc34vo0extjvyi2xyoce7 unique (cusNumber);

    alter table if exists dbo.Entity 
       add constraint UK_53q2cqaunqrmk3crdradt5ymx unique (entName);

    alter table if exists dbo.Item 
       add constraint UK_k8wciy6sp0e8jmlsp5p309t0x unique (itmIdent);

    alter table if exists dbo.ItemGroup 
       add constraint UK_9dole6hf8puv9k5b5fr2dlmee unique (itgNumber);

    alter table if exists dbo.LabelAssignment 
       add constraint UK_k2kmjh0qxss0anvsquvk11wfj unique (claId);

    alter table if exists dbo.Language 
       add constraint UK_mdj5ljxyol2jfu8t8wqopymof unique (lngCode);

    alter table if exists dbo.PaymentCondition 
       add constraint UK_4str8ccdjgg5my6137eqhrn79 unique (pacCode);

    alter table if exists dbo.Periode 
       add constraint UKosv0a3ls28ye45x1rdsbpog65 unique (perYear, perMonth, percsaId);

    alter table if exists dbo.Project 
       add constraint UK_6k1ppn211obo547xptabmwjm2 unique (proName);

    alter table if exists dbo.RowImage 
       add constraint UKcm5e9ph652m3n9i819eylhmsa unique (rimobjId, rimType, rimNumber);

    alter table if exists dbo.RowObject 
       add constraint UKjsl99se62wopegmf8icba92im unique (objentId, objRowId);

    alter table if exists dbo.RowParameter 
       add constraint UKg3s77e40twagh1ynfuermrqlt unique (prmobjId, prmGroup, prmSubGroup, prmKey);

    alter table if exists dbo.RowText 
       add constraint UKbqscfcorv91jfrmpus2a3bt5f unique (txtobjId, txtlngId, txtNumber);

    alter table if exists dbo.Vat 
       add constraint UK_9hwxcnls7uetgh3gp01btninm unique (vatSign);

    alter table if exists dbo.VatLine 
       add constraint UK_cof8c3yo3qaqsmjdyj1j5ri3t unique (vanRemark);

    alter table if exists dbo."Order" 
       add constraint FKplwjii0pak6mcypqtco7eqas 
       foreign key (ordcusId) 
       references dbo.Customer;

    alter table if exists dbo."Order" 
       add constraint FKswgetjxisf6hbol7899tt29eu 
       foreign key (ordpacId) 
       references dbo.PaymentCondition;

    alter table if exists dbo."Order" 
       add constraint FKr3r09jthl2gha9gvsr45a87mb 
       foreign key (ordproId) 
       references dbo.Project;

    alter table if exists dbo.Activity 
       add constraint FKayaldwu68nf8fnk0erdvvp891 
       foreign key (actcsaId) 
       references dbo.CostAccount;

    alter table if exists dbo.Activity 
       add constraint FKkek1hducxxrrmv1b3i3wrlwad 
       foreign key (actcusId) 
       references dbo.Customer;

    alter table if exists dbo.Address 
       add constraint FKo4b4ni9esdkf2gxh4ronvdg0o 
       foreign key (adrcusId) 
       references dbo.Customer;

    alter table if exists dbo.AppUser 
       add constraint FKshdwc70aitoab89vecp6d78v2 
       foreign key (usrcsaId) 
       references dbo.CostAccount;

    alter table if exists dbo.AppUser 
       add constraint FKfspukq38jjtr5wkmml6xwwcre 
       foreign key (usrcusId) 
       references dbo.Customer;

    alter table if exists dbo.Bank 
       add constraint FKs3pltrlkxw7b7c4l95wsvkdce 
       foreign key (bnkctyId) 
       references dbo.City;

    alter table if exists dbo.ContactRelation 
       add constraint FK4njyj4gac6fb94mkho0v1i2it 
       foreign key (corcusIdTypeOne) 
       references dbo.Customer;

    alter table if exists dbo.ContactRelation 
       add constraint FKo2y59r2h7xc4y3tjqxama665h 
       foreign key (corcusIdTypeTwo) 
       references dbo.Customer;

    alter table if exists dbo.CostAccount 
       add constraint FKeu85thspq4uoq7jet2lq0x3i3 
       foreign key (csacsaId) 
       references dbo.CostAccount;

    alter table if exists dbo.Customer 
       add constraint FKkg61fkvbpedq31lk4kwudjjnd 
       foreign key (cusctyId) 
       references dbo.City;

    alter table if exists dbo.Customer 
       add constraint FK5htte45us6pa0y87mvom4epio 
       foreign key (cuspacId) 
       references dbo.PaymentCondition;

    alter table if exists dbo.CustomerLink 
       add constraint FK3msgdiof651n42phrind7tjep 
       foreign key (cnkcusId) 
       references dbo.Customer;

    alter table if exists dbo.Expense 
       add constraint FKlut9796pgo41ihffvs9stioud 
       foreign key (expperId) 
       references dbo.Periode;

    alter table if exists dbo.Expense 
       add constraint FKo0qhmtfude93xigiku793872x 
       foreign key (expproId) 
       references dbo.Project;

    alter table if exists dbo.Expense 
       add constraint FK9lalkjbvnpx7g1si9tvfs8ubl 
       foreign key (expvatId) 
       references dbo.Vat;

    alter table if exists dbo.ExpenseTemplate 
       add constraint FKiu05bwl2jdsprd82bqin6j3cc 
       foreign key (extcsaId) 
       references dbo.CostAccount;

    alter table if exists dbo.ExpenseTemplate 
       add constraint FKkbj8mvynl85watqeqwitve7r5 
       foreign key (extproId) 
       references dbo.Project;

    alter table if exists dbo.ExpenseTemplate 
       add constraint FKimpjr6oje0ucr6ro0jspl8j3c 
       foreign key (extvatId) 
       references dbo.Vat;

    alter table if exists dbo.Item 
       add constraint FK1s8selkavtya28xcp1r6shoty 
       foreign key (itmitgId) 
       references dbo.ItemGroup;

    alter table if exists dbo.Item 
       add constraint FKkdx5xso1brwjdpf6wil9812gq 
       foreign key (itmvatId) 
       references dbo.Vat;

    alter table if exists dbo.ItemGroup 
       add constraint FKkbv5bsfch0sejicjrlg3t1uu9 
       foreign key (itgitgParent) 
       references dbo.ItemGroup;

    alter table if exists dbo.LabelAssignment 
       add constraint FKmgqsndeo24exqy1npwvns350e 
       foreign key (clacusId) 
       references dbo.Customer;

    alter table if exists dbo.LabelAssignment 
       add constraint FK5xw1bpqakl87711bf0bwldxt9 
       foreign key (clacldId) 
       references dbo.LabelDefinition;

    alter table if exists dbo.OrderLine 
       add constraint FKqu61njj04mn0xm1pl8rsr0mx4 
       foreign key (odlcsaId) 
       references dbo.CostAccount;

    alter table if exists dbo.OrderLine 
       add constraint FKjpsytvd0eg1tu7bxa0udh698y 
       foreign key (odlitmId) 
       references dbo.Item;

    alter table if exists dbo.OrderLine 
       add constraint FK61i676xgj92wnbtf5a1b4c5i5 
       foreign key (odlordId) 
       references dbo."Order";

    alter table if exists dbo.OrderLine 
       add constraint FKnfn4wnpxv162ef1auhrg0jphb 
       foreign key (odlvatId) 
       references dbo.Vat;

    alter table if exists dbo.Periode 
       add constraint FK6up66u5d9mlqihq33mcddsxw0 
       foreign key (percsaId) 
       references dbo.CostAccount;

    alter table if exists dbo.Project 
       add constraint FKn6x2yy5q94m1p9ydce43fssdx 
       foreign key (proadrId) 
       references dbo.Address;

    alter table if exists dbo.Project 
       add constraint FK2g3egvhhee7fwibmyiqfdu5gf 
       foreign key (procsaId) 
       references dbo.CostAccount;

    alter table if exists dbo.Project 
       add constraint FKmf4hgh9atck9vrtxct9l0eiwv 
       foreign key (procusId) 
       references dbo.Customer;

    alter table if exists dbo.Project 
       add constraint FKd11itn9i9hr9vf7w7knyiu789 
       foreign key (proproId) 
       references dbo.Project;

    alter table if exists dbo.Project 
       add constraint FKlufn9kpu2qdo0desitru948f6 
       foreign key (provatId) 
       references dbo.Vat;

    alter table if exists dbo.ProjectLine 
       add constraint FKeenhps4f7ok914tt9v55vr8en 
       foreign key (prlperId) 
       references dbo.Periode;

    alter table if exists dbo.ProjectLine 
       add constraint FK2vku87tsgm6sq87haxk5d4pnc 
       foreign key (prlproId) 
       references dbo.Project;

    alter table if exists dbo.ProjectLineTemplate 
       add constraint FK9rvhwvssh3f01lft2ls6ldma7 
       foreign key (prtcsaId) 
       references dbo.CostAccount;

    alter table if exists dbo.ProjectLineTemplate 
       add constraint FK8wc8aq8oxbc0mnsdi246g7v38 
       foreign key (prtproId) 
       references dbo.Project;

    alter table if exists dbo.ResPlanning 
       add constraint FKekxje6w8l97f460lvgylhkmwc 
       foreign key (rspcsaId) 
       references dbo.CostAccount;

    alter table if exists dbo.ResPlanning 
       add constraint FK7eqsjdphketps8rxy6y4kmd9b 
       foreign key (rspproId) 
       references dbo.Project;

    alter table if exists dbo.RowImage 
       add constraint FKgq1qucp8ks82ph8656hlv0vi7 
       foreign key (rimobjId) 
       references dbo.RowObject;

    alter table if exists dbo.RowLabel 
       add constraint FKqrythi1afaxt2qh6xai7gjnqw 
       foreign key (lbllngId) 
       references dbo.Language;

    alter table if exists dbo.RowLabel 
       add constraint FKqkygsm9u4o97ya3e97duolu8y 
       foreign key (lblobjId) 
       references dbo.RowObject;

    alter table if exists dbo.RowObject 
       add constraint FK1a2td2ry9d33gkq3upnapgspe 
       foreign key (objentId) 
       references dbo.Entity;

    alter table if exists dbo.RowParameter 
       add constraint FK4bdob6inosooprv8yd9hkcs8f 
       foreign key (prmobjId) 
       references dbo.RowObject;

    alter table if exists dbo.RowRelation 
       add constraint FKfe9m1q7893gjhy92w2orhymmt 
       foreign key (relobjId_Source) 
       references dbo.RowObject;

    alter table if exists dbo.RowRelation 
       add constraint FKtcvspuoutd5iiww3lg9mjamc6 
       foreign key (relobjId_Target) 
       references dbo.RowObject;

    alter table if exists dbo.RowText 
       add constraint FKpxpglu7f3w3nj7q11ynlm9h1p 
       foreign key (txtlngId) 
       references dbo.Language;

    alter table if exists dbo.RowText 
       add constraint FKh7r9a6lfd0ma3lj1oi8xhjffr 
       foreign key (txtobjId) 
       references dbo.RowObject;

    alter table if exists dbo.StateCode 
       add constraint FKjm247rb954uhy4mdew25ti082 
       foreign key (stcentId) 
       references dbo.Entity;

    alter table if exists dbo.VatLine 
       add constraint FKoeknuxndnncgyxcoy5gw1qpnn 
       foreign key (vanvatId) 
       references dbo.Vat;
