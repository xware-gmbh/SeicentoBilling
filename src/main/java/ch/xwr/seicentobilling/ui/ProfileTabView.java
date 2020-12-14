
package ch.xwr.seicentobilling.ui;

import java.time.LocalDate;
import java.time.Month;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.rapidclipse.framework.server.ui.StartsWithIgnoreCaseItemFilter;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.DataProvider;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.LovState.Theme;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.auth.SeicentoUser;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.AppUserDAO;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.entities.AppUser;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;


public class ProfileTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ProfileTabView.class);
	
	/**
	 *
	 */
	public ProfileTabView()
	{
		super();
		this.initUI();

		this.comboBoxState.setItems(LovState.State.values());
		this.cmbThemeDesktop.setItems(LovState.Theme.values());
		this.cmbThemeMobile.setItems(LovState.Theme.values());
		this.icon.getElement().setProperty("title", "Damit Änderungen wirksam werden muss man sich neu anmelden!");
		
		final AppUser bean = this.getAppUserBean();

		if(bean != null)
		{
			this.binder.setBean(bean);
		}
		else
		{
			this.horizontalLayout2.setEnabled(false);
			ProfileTabView.LOG.error("No AppUser found in Database!");
		}

		this.setROFields();

	}
	
	private void setROFields()
	{
		String lm = "Authentication with Database";

		if(Seicento.getLoginMethod().equals("azure"))
		{
			lm = "Authentication with azure ad";

			this.txtUsername.setEnabled(false);
			this.txtUsrRoles.setEnabled(false);
			this.cmdSetPassword.setEnabled(false);
		}
		this.lblLoginMode.setText(lm);
	}
	
	private AppUser getAppUserBean()
	{
		final SeicentoUser su   = Seicento.getSeicentoUser();
		AppUser            bean = su.getDbUser();
		
		if(bean != null)
		{
			bean = new AppUserDAO().find(bean.getUsrId()); // reread possible changes
		}
		return bean;
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSave}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_onClick(final ClickEvent<Button> event)
	{
		if(SeicentoCrud.doSave(this.binder, new AppUserDAO()))
		{
			try
			{
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getUsrId(),
					this.binder.getBean().getClass().getSimpleName());
			}
			catch(final Exception e)
			{
				ProfileTabView.LOG.error("could not save ObjRoot", e);
			}
		}

		this.setROFields();
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReset2}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset2_onClick(final ClickEvent<Button> event)
	{
		final AppUser bean = this.getAppUserBean();

		if(bean != null)
		{
			this.binder.setBean(bean);
		}
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdApplyTheme}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdApplyTheme_onClick(final ClickEvent<Button> event)
	{
		final LovState.Theme th = this.cmbThemeDesktop.getValue();
		if(th.equals(LovState.Theme.dark))
		{
			this.getElement().executeJs("document.querySelector('html').setAttribute('theme', 'dark');");
		}
		if(th.equals(LovState.Theme.light))
		{
			this.getElement().executeJs("document.querySelector('html').setAttribute('theme', 'light');");
		}
		
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSetPassword}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSetPassword_onClick(final ClickEvent<Button> event)
	{
		final AppUser bean = this.getAppUserBean();
		UI.getCurrent().getSession().setAttribute("appuserbean", bean);
		final Dialog win = PasswordPopup.getPopupWindow();
		win.open();
		
	}

	/**
	 * Event handler delegate method for the {@link VerticalLayout} {@link #gridLayoutProfile}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void gridLayoutProfile_onAttach(final AttachEvent event)
	{
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.tabs               = new Tabs();
		this.tab                = new Tab();
		this.gridLayoutProfile  = new VerticalLayout();
		this.formLayout         = new FormLayout();
		this.formItem           = new FormItem();
		this.lblLoginMode       = new Label();
		this.formItem2          = new FormItem();
		this.icon               = new Icon(VaadinIcon.INFO_CIRCLE);
		this.formItem3          = new FormItem();
		this.lblUsername        = new Label();
		this.txtUsername        = new TextField();
		this.formItem5          = new FormItem();
		this.cmdSetPassword     = new Button();
		this.formItem4          = new FormItem();
		this.lblUsrRoles        = new Label();
		this.txtUsrRoles        = new TextField();
		this.formItem6          = new FormItem();
		this.lblUsrFullName     = new Label();
		this.txtUsrFullName     = new TextField();
		this.formItem7          = new FormItem();
		this.lblUsrThemeDesktop = new Label();
		this.cmbThemeDesktop    = new ComboBox<>();
		this.formItem8          = new FormItem();
		this.lblUsrThemeMobile  = new Label();
		this.cmbThemeMobile     = new ComboBox<>();
		this.formItem9          = new FormItem();
		this.lblUsrLanguage     = new Label();
		this.txtUsrLanguage     = new TextField();
		this.formItem10         = new FormItem();
		this.lblUsrCountry      = new Label();
		this.txtUsrCountry      = new TextField();
		this.formItem11         = new FormItem();
		this.lblUsrTimeZone     = new Label();
		this.txtUsrTimeZone     = new TextField();
		this.formItem12         = new FormItem();
		this.lblCostAccount     = new Label();
		this.cmbCostAccount     = new ComboBox<>();
		this.formItem13         = new FormItem();
		this.lblCustomer        = new Label();
		this.cmbCustomer        = new ComboBox<>();
		this.formItem14         = new FormItem();
		this.lblUsrValidFrom    = new Label();
		this.dateUsrValidFrom   = new DatePicker();
		this.formItem15         = new FormItem();
		this.lblUsrValidTo      = new Label();
		this.dateUsrValidTo     = new DatePicker();
		this.formItem16         = new FormItem();
		this.lblUsrState        = new Label();
		this.comboBoxState      = new ComboBox<>();
		this.horizontalLayout   = new HorizontalLayout();
		this.cmdSave            = new Button();
		this.cmdReset2          = new Button();
		this.horizontalLayout2  = new HorizontalLayout();
		this.cmdApplyTheme      = new Button();
		this.binder             = new BeanValidationBinder<>(AppUser.class);

		this.tab.setLabel("Benutzer");
		this.gridLayoutProfile.setPadding(false);
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 3, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.lblLoginMode.setText("Label");
		this.formItem2.getElement().setAttribute("colspan", "2");
		this.lblUsername.setText("Login Name");
		this.formItem5.getElement().setAttribute("colspan", "2");
		this.cmdSetPassword.setText("Passwort...");
		this.cmdSetPassword.setIcon(VaadinIcon.PASSWORD.create());
		this.lblUsrRoles.setText("Roles");
		this.formItem6.getElement().setAttribute("colspan", "2");
		this.lblUsrFullName.setText("Name");
		this.lblUsrThemeDesktop.setText("Theme Desktop");
		this.cmbThemeDesktop.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblUsrThemeMobile.setText("Theme Mobile");
		this.cmbThemeMobile.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblUsrLanguage.setText("Language");
		this.lblUsrCountry.setText("Country");
		this.lblUsrTimeZone.setText("TimeZone");
		this.lblCostAccount.setText("CostAccount");
		this.cmbCostAccount.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.cmbCostAccount::getItemLabelGenerator),
			DataProvider.ofCollection(new CostAccountDAO().findAll()));
		this.cmbCostAccount.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CostAccount::getCsaCode));
		this.lblCustomer.setText("Contact");
		this.cmbCustomer.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbCustomer::getItemLabelGenerator),
			DataProvider.ofCollection(new CustomerDAO().findAll()));
		this.cmbCustomer.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Customer::getCusName));
		this.lblUsrValidFrom.setText("Valid From");
		this.lblUsrValidTo.setText("Valid To");
		this.lblUsrState.setText("Status");
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText("Speichern");
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset2.setText("Verwerfen");
		this.cmdReset2.setIcon(IronIcons.UNDO.create());
		this.cmdApplyTheme.setText("Theme anwenden");
		this.cmdApplyTheme.setIcon(VaadinIcon.DASHBOARD.create());

		this.binder.forField(this.txtUsername).withNullRepresentation("").bind("username");
		this.binder.forField(this.txtUsrRoles).withNullRepresentation("").bind("usrRoles");
		this.binder.forField(this.txtUsrFullName).withNullRepresentation("").bind("usrFullName");
		this.binder.forField(this.cmbThemeDesktop).bind("usrThemeDesktop");
		this.binder.forField(this.cmbThemeMobile).bind("usrThemeMobile");
		this.binder.forField(this.txtUsrLanguage).withNullRepresentation("").bind("usrLanguage");
		this.binder.forField(this.txtUsrCountry).withNullRepresentation("").bind("usrCountry");
		this.binder.forField(this.txtUsrTimeZone).withNullRepresentation("").bind("usrTimeZone");
		this.binder.forField(this.cmbCostAccount).bind("costAccount");
		this.binder.forField(this.cmbCustomer).bind("customer");
		this.binder.forField(this.dateUsrValidFrom).withNullRepresentation(LocalDate.of(2020, Month.DECEMBER, 7))
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("usrValidFrom");
		this.binder.forField(this.dateUsrValidTo).withNullRepresentation(LocalDate.of(2020, Month.DECEMBER, 7))
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("usrValidTo");
		this.binder.forField(this.comboBoxState).bind("usrState");

		this.tabs.add(this.tab);
		this.lblLoginMode.setWidthFull();
		this.lblLoginMode.setHeight(null);
		this.formItem.add(this.lblLoginMode);
		this.formItem2.add(this.icon);
		this.lblUsername.setSizeUndefined();
		this.lblUsername.getElement().setAttribute("slot", "label");
		this.txtUsername.setWidthFull();
		this.txtUsername.setHeight(null);
		this.formItem3.add(this.lblUsername, this.txtUsername);
		this.cmdSetPassword.setWidth("20%");
		this.cmdSetPassword.setHeight(null);
		this.formItem5.add(this.cmdSetPassword);
		this.lblUsrRoles.setSizeUndefined();
		this.lblUsrRoles.getElement().setAttribute("slot", "label");
		this.txtUsrRoles.setWidthFull();
		this.txtUsrRoles.setHeight(null);
		this.formItem4.add(this.lblUsrRoles, this.txtUsrRoles);
		this.lblUsrFullName.setSizeUndefined();
		this.lblUsrFullName.getElement().setAttribute("slot", "label");
		this.txtUsrFullName.setWidth("40%");
		this.txtUsrFullName.setHeight(null);
		this.formItem6.add(this.lblUsrFullName, this.txtUsrFullName);
		this.lblUsrThemeDesktop.setSizeUndefined();
		this.lblUsrThemeDesktop.getElement().setAttribute("slot", "label");
		this.cmbThemeDesktop.setWidthFull();
		this.cmbThemeDesktop.setHeight(null);
		this.formItem7.add(this.lblUsrThemeDesktop, this.cmbThemeDesktop);
		this.lblUsrThemeMobile.setSizeUndefined();
		this.lblUsrThemeMobile.getElement().setAttribute("slot", "label");
		this.cmbThemeMobile.setWidthFull();
		this.cmbThemeMobile.setHeight(null);
		this.formItem8.add(this.lblUsrThemeMobile, this.cmbThemeMobile);
		this.lblUsrLanguage.setSizeUndefined();
		this.lblUsrLanguage.getElement().setAttribute("slot", "label");
		this.txtUsrLanguage.setWidthFull();
		this.txtUsrLanguage.setHeight(null);
		this.formItem9.add(this.lblUsrLanguage, this.txtUsrLanguage);
		this.lblUsrCountry.setSizeUndefined();
		this.lblUsrCountry.getElement().setAttribute("slot", "label");
		this.txtUsrCountry.setWidthFull();
		this.txtUsrCountry.setHeight(null);
		this.formItem10.add(this.lblUsrCountry, this.txtUsrCountry);
		this.lblUsrTimeZone.setSizeUndefined();
		this.lblUsrTimeZone.getElement().setAttribute("slot", "label");
		this.txtUsrTimeZone.setWidthFull();
		this.txtUsrTimeZone.setHeight(null);
		this.formItem11.add(this.lblUsrTimeZone, this.txtUsrTimeZone);
		this.lblCostAccount.setSizeUndefined();
		this.lblCostAccount.getElement().setAttribute("slot", "label");
		this.cmbCostAccount.setWidthFull();
		this.cmbCostAccount.setHeight(null);
		this.formItem12.add(this.lblCostAccount, this.cmbCostAccount);
		this.lblCustomer.setSizeUndefined();
		this.lblCustomer.getElement().setAttribute("slot", "label");
		this.cmbCustomer.setWidthFull();
		this.cmbCustomer.setHeight(null);
		this.formItem13.add(this.lblCustomer, this.cmbCustomer);
		this.lblUsrValidFrom.setSizeUndefined();
		this.lblUsrValidFrom.getElement().setAttribute("slot", "label");
		this.dateUsrValidFrom.setWidthFull();
		this.dateUsrValidFrom.setHeight(null);
		this.formItem14.add(this.lblUsrValidFrom, this.dateUsrValidFrom);
		this.lblUsrValidTo.setSizeUndefined();
		this.lblUsrValidTo.getElement().setAttribute("slot", "label");
		this.dateUsrValidTo.setWidthFull();
		this.dateUsrValidTo.setHeight(null);
		this.formItem15.add(this.lblUsrValidTo, this.dateUsrValidTo);
		this.lblUsrState.setSizeUndefined();
		this.lblUsrState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem16.add(this.lblUsrState, this.comboBoxState);
		this.formLayout.add(this.formItem, this.formItem2, this.formItem3, this.formItem5, this.formItem4,
			this.formItem6,
			this.formItem7, this.formItem8, this.formItem9, this.formItem10, this.formItem11, this.formItem12,
			this.formItem13, this.formItem14, this.formItem15, this.formItem16);
		this.cmdSave.setSizeUndefined();
		this.cmdReset2.setSizeUndefined();
		this.horizontalLayout2.setWidth("200px");
		this.horizontalLayout2.setHeight("20px");
		this.cmdApplyTheme.setSizeUndefined();
		this.horizontalLayout.add(this.cmdSave, this.cmdReset2, this.horizontalLayout2, this.cmdApplyTheme);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.cmdSave);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.cmdReset2);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.cmdApplyTheme);
		this.formLayout.setSizeFull();
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("40px");
		this.gridLayoutProfile.add(this.formLayout, this.horizontalLayout);
		this.tabs.setWidthFull();
		this.tabs.setHeight(null);
		this.gridLayoutProfile.setSizeFull();
		this.add(this.tabs, this.gridLayoutProfile);
		this.setSizeFull();

		this.tabs.setSelectedIndex(0);

		this.gridLayoutProfile.addAttachListener(this::gridLayoutProfile_onAttach);
		this.cmdSetPassword.addClickListener(this::cmdSetPassword_onClick);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset2.addClickListener(this::cmdReset2_onClick);
		this.cmdApplyTheme.addClickListener(this::cmdApplyTheme_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private Tab                           tab;
	private ComboBox<Customer>            cmbCustomer;
	private VerticalLayout                gridLayoutProfile;
	private HorizontalLayout              horizontalLayout, horizontalLayout2;
	private Label                         lblLoginMode, lblUsername, lblUsrRoles, lblUsrFullName, lblUsrThemeDesktop,
		lblUsrThemeMobile, lblUsrLanguage, lblUsrCountry, lblUsrTimeZone, lblCostAccount, lblCustomer, lblUsrValidFrom,
		lblUsrValidTo, lblUsrState;
	private Tabs                          tabs;
	private FormItem                      formItem, formItem2, formItem3, formItem5, formItem4, formItem6, formItem7,
		formItem8, formItem9, formItem10, formItem11, formItem12, formItem13, formItem14, formItem15, formItem16;
	private ComboBox<Theme>               cmbThemeDesktop, cmbThemeMobile;
	private FormLayout                    formLayout;
	private Button                        cmdSetPassword, cmdSave, cmdReset2, cmdApplyTheme;
	private ComboBox<State>               comboBoxState;
	private DatePicker                    dateUsrValidFrom, dateUsrValidTo;
	private BeanValidationBinder<AppUser> binder;
	private Icon                          icon;
	private TextField                     txtUsername, txtUsrRoles, txtUsrFullName, txtUsrLanguage, txtUsrCountry,
		txtUsrTimeZone;
	private ComboBox<CostAccount>         cmbCostAccount;
	// </generated-code>
	
}
