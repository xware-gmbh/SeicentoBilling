package ch.xwr.seicentobilling.ui.phone;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.dal.DAOs;
import com.xdev.res.ApplicationResource;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevCheckBox;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevMenuBar;
import com.xdev.ui.XdevMenuBar.XdevMenuItem;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.ui.navigation.Navigation;
import com.xdev.ui.navigation.NavigationParameter;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.dal.ExpenseTemplateDAO;
import ch.xwr.seicentobilling.dal.LovAccountDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.ExpenseTemplate;
import ch.xwr.seicentobilling.entities.Expense_;
import ch.xwr.seicentobilling.entities.LovAccount;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Project_;
import ch.xwr.seicentobilling.entities.RowObject;
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.entities.Vat_;

public class ExpenseView extends XdevView {

	@NavigationParameter
	private Expense expense;
	@NavigationParameter
	private Periode periode;
	/**
	 *
	 */
	public ExpenseView() {
		super();
		this.initUI();

		// State
		//this.comboBoxState.addItems((Object[]) LovState.State.values());
		this.comboBoxUnit.addItems((Object[]) LovState.ExpUnit.values());
		this.comboBoxGeneric.addItems((Object[]) LovState.ExpType.values());

		this.panel.getContent().setSizeUndefined();
	}

//	private RowImage getRowImageBean() {
//		final RowObjectManager man = new RowObjectManager();
//		final Expense exp = this.fieldGroup.getItemDataSource().getBean();
//
//		final RowObject obj = man.getRowObject(exp.getClass().getSimpleName(), exp.getExpId());
//		RowImage bean = null;
//
//		if (obj != null && obj.getObjId() > 0) {
//			bean = new RowImage();
//			bean.setRimState(LovState.State.active);
//			bean.setRowObject(obj);
//			bean.setRimNumber(850);
//			bean.setRimType((short) 2);
//		}
//		return bean;
//	}

	@Override
	public void enter(final ViewChangeListener.ViewChangeEvent event) {
		super.enter(event);

		this.expense = Navigation.getParameter(event, "expense", Expense.class);
		this.periode = Navigation.getParameter(event, "periode", Periode.class);

		if (this.expense != null) {
			this.fieldGroup.setItemDataSource(this.expense);
			postLoadAccountAction(this.expense);
		}

		checkTemplates();

		if (this.expense.getExpId() == null || this.expense.getExpId().floatValue() < 1) {
			this.mnuUpload.setEnabled(false);
		}
	}

	private void checkTemplates() {
		final ExpenseTemplateDAO dao = new ExpenseTemplateDAO();
		final List<ExpenseTemplate> lst = dao.findByCostAccount(Seicento.getLoggedInCostAccount());

		XdevMenuItem item = null;

		for (int i = 1; i < 11; i++) {
			item = getMnItem(i);
			item.setEnabled(false);
			item.setVisible(false);
		}

		if (lst == null)
		{
			return;	//not found
		}

		for (final Iterator<ExpenseTemplate> iterator = lst.iterator(); iterator.hasNext();) {
			final ExpenseTemplate tpl = iterator.next();
			final int nbr = tpl.getExtKeyNumber();
			item = getMnItem(nbr);

			item.setEnabled(true);
			item.setVisible(true);
			item.setCaption("" + nbr + ": " + tpl.getProject().getProName());
		}

	}

	private XdevMenuItem getMnItem(final int icount) {
		switch (icount) {
			case 1: return this.mnuTemplate1;
			case 2: return this.mnuTemplate2;
			case 3: return this.mnuTemplate3;
			case 4: return this.mnuTemplate4;
			case 5: return this.mnuTemplate5;
			case 6: return this.mnuTemplate6;
			case 7: return this.mnuTemplate7;
			case 8: return this.mnuTemplate8;
			case 9: return this.mnuTemplate9;
			case 10: return this.mnuTemplate10;
		}

		return null;
	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReset}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_buttonClick(final Button.ClickEvent event) {
		this.fieldGroup.discard();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		try {
			preSaveAccountAction();
			this.fieldGroup.save();
			final RowObjectManager man = new RowObjectManager();
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getExpId(),
					this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());
			goBack();

		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void preSaveAccountAction() {
		final LovAccount lov = this.comboBoxAccount.getSelectedItem().getBean();
		if (lov != null) {
			this.fieldGroup.getItemDataSource().getBean().setExpAccount(lov.getId());
		}

	}

	private void postLoadAccountAction(final Expense bean) {
		if (bean.getExpAccount() == null) {
			return;
		}

		// final boolean exist = this.comboBoxAccount.containsId(lov);
		// funktioniert auf keine Weise....

		final Collection<?> col1 = this.comboBoxAccount.getItemIds();
		for (final Iterator<?> iterator = col1.iterator(); iterator.hasNext();) {
			final LovAccount lovBean = (LovAccount) iterator.next();
			if (lovBean.getId().equals(bean.getExpAccount())) {
				this.comboBoxAccount.select(lovBean);
				break;
			}
		}

	}


	private void goBack() {
		Navigation.to("expenseListView").parameter("periode", this.periode).navigate();
	}
	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdBack}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdBack_buttonClick(final Button.ClickEvent event) {
		goBack();
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuDeleteItem}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuDeleteItem_menuSelected(final MenuBar.MenuItem selectedItem) {
		ConfirmDialog.show(getUI(), "Datensatz löschen", "Wirklich löschen?", new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				if (retval == null) {
					retval = "cmdCancel";
				}

				if (retval.equals("cmdOk")) {
					doDelete();
				}
			}

			private void doDelete() {
				final Expense bean = ExpenseView.this.fieldGroup.getItemDataSource().getBean();
				if (bean.getExpId() > 0) {
					// Update RowObject
					final RowObjectManager man = new RowObjectManager();
					man.deleteObject(bean.getExpId(), bean.getClass().getSimpleName());
					// Delete Record
					final ExpenseDAO dao = new ExpenseDAO();
					dao.remove(bean);
				}

				goBack();
			}

		});


	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuResetItem}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuResetItem_menuSelected(final MenuBar.MenuItem selectedItem) {
		this.fieldGroup.discard();
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate1}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate1_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(1);
	}

	private void loadTemplate(final int iKey) {
		final Expense line = this.fieldGroup.getItemDataSource().getBean();

		final ExpenseTemplateDAO dao = new ExpenseTemplateDAO();
		final ExpenseTemplate tpl = dao.findByKeyNumber(line.getPeriode().getCostAccount(), iKey);

		if (tpl == null)
		 {
			return;	//not found
		}

		line.setExpAccount(tpl.getExtAccount());
		line.setExpAmount(tpl.getExtAmount());
		line.setExpFlagCostAccount(tpl.getExtFlagCostAccount());
		line.setExpFlagGeneric(tpl.getExtFlagGeneric());
		line.setExpQuantity(tpl.getExtQuantity());
		line.setExpState(tpl.getExtState());
		line.setExpText(tpl.getExtText());
		line.setExpUnit(tpl.getExtUnit());
		line.setProject(tpl.getProject());
		line.setVat(tpl.getVat());


		this.fieldGroup.setItemDataSource(line);
		//setROFields();

		postLoadAccountAction(line);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate2}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate2_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(2);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate3}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate3_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(3);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate4}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate4_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(4);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate5}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate5_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(5);
	}
	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate6}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate6_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(6);
	}
	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate7}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate7_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(7);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate8}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate8_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(8);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate9}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate9_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(9);
	}
	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate10}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate10_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(0);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDefault1}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDefault1_buttonClick(final Button.ClickEvent event) {
		loadTemplate(1);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #menuText}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void menuText_menuSelected(final MenuBar.MenuItem selectedItem) {
		if (this.cmbProject.getSelectedItem() == null) {
			return;
		}

		final Project pro = this.cmbProject.getSelectedItem().getBean();
		UI.getCurrent().getSession().setAttribute("project", pro);
		UI.getCurrent().getSession().setAttribute("target", 1);

		popupTextTemplate();

	}

	private void popupTextTemplate() {
		final Window win = TextListPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				final String reason = (String) UI.getCurrent().getSession().getAttribute("textValue");

				if (retval == null) {
					retval = "cmdCancel";
				}
				if (retval.equals("cmdDone")) {
					ExpenseView.this.txtExpText.setValue(reason);
				}

			}
		});

		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuUpload}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuUpload_menuSelected(final MenuBar.MenuItem selectedItem) {
		if (this.expense.getExpId() == null) {
			return;
		}

		final RowObjectManager man = new RowObjectManager();
		final RowObject obj = man.getRowObject(this.expense.getClass().getSimpleName(), this.expense.getExpId());
		UI.getCurrent().getSession().setAttribute("RowObject", obj);

		popupAttachments();

	}

	private void popupAttachments() {
		final Window win = AttachmentPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				String retval = UI.getCurrent().getSession().getAttribute(String.class);

				if (retval == null) {
					retval = "cmdCancel";
				}
				if (retval.equals("cmdDone")) {
					//ExpenseView.this.txtExpText.setValue(reason);
				}

			}
		});

		this.getUI().addWindow(win);

	}


	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalLayout = new XdevVerticalLayout();
		this.horizontalLayoutTitle = new XdevHorizontalLayout();
		this.menuBarLeftTop = new XdevMenuBar();
		this.mnuOption = this.menuBarLeftTop.addItem("Optionen", null);
		this.mnuUpload = this.mnuOption.addItem("Dokument hochladen...", null);
		this.mnuSeperator2 = this.mnuOption.addSeparator();
		this.mnuDefaults = this.mnuOption.addItem("Vorlage", null);
		this.mnuTemplate1 = this.mnuDefaults.addItem("Spesen", null);
		this.mnuTemplate2 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate3 = this.mnuDefaults.addItem("Vorlagen Rapport", null);
		this.mnuTemplate4 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate5 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate6 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate7 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate8 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate9 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate10 = this.mnuDefaults.addItem("Rapporte", null);
		this.menuText = this.mnuOption.addItem("Text...", null);
		this.mnuSeperator = this.mnuOption.addSeparator();
		this.mnuResetItem = this.mnuOption.addItem("Zurücksetzen", null);
		this.mnuDeleteItem = this.mnuOption.addItem("Löschen", null);
		this.label = new XdevLabel();
		this.cmdBack = new XdevButton();
		this.panel = new XdevPanel();
		this.form = new XdevGridLayout();
		this.lblExpDate = new XdevLabel();
		this.dateExpDate = new XdevPopupDateField();
		this.lblExpAmount = new XdevLabel();
		this.txtExpAmount = new XdevTextField();
		this.lblExpText = new XdevLabel();
		this.txtExpText = new XdevTextField();
		this.lblProject = new XdevLabel();
		this.cmbProject = new XdevComboBox<>();
		this.lblVat = new XdevLabel();
		this.cmbVat = new XdevComboBox<>();
		this.lblExpAccount = new XdevLabel();
		this.comboBoxAccount = new XdevComboBox<>();
		this.lblExpFlagCostAccount = new XdevLabel();
		this.chkExpFlagCostAccount = new XdevCheckBox();
		this.lblExpFlagGeneric = new XdevLabel();
		this.comboBoxGeneric = new XdevComboBox<>();
		this.lblExpUnit = new XdevLabel();
		this.comboBoxUnit = new XdevComboBox<>();
		this.lblExpQuantity = new XdevLabel();
		this.txtExpQuantity = new XdevTextField();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.cmdDefault1 = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Expense.class);

		this.verticalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayoutTitle.setStyleName("dark");
		this.horizontalLayoutTitle.setMargin(new MarginInfo(false));
		this.menuBarLeftTop.setStyleName("large large-icons");
		this.mnuOption.setIcon(FontAwesome.NAVICON);
		this.mnuOption.setStyleName("large-icons");
		this.mnuUpload.setIcon(FontAwesome.UPLOAD);
		this.mnuDefaults.setIcon(FontAwesome.BOOKMARK);
		this.menuText.setIcon(FontAwesome.LIST_ALT);
		this.mnuResetItem.setIcon(FontAwesome.UNDO);
		this.mnuResetItem.setCheckable(true);
		this.mnuDeleteItem.setIcon(FontAwesome.MINUS_CIRCLE);
		this.mnuDeleteItem.setCheckable(true);
		this.label.setStyleName("colored bold");
		this.label.setValue("Spesen erfassen");
		this.cmdBack.setIcon(
				new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/greenarrow_left32.png"));
		this.cmdBack.setCaption(StringResourceUtils.optLocalizeString("{$cmdBack.caption}", this));
		this.form.setMargin(new MarginInfo(false, true, true, true));
		this.lblExpDate.setValue(StringResourceUtils.optLocalizeString("{$lblExpDate.value}", this));
		this.dateExpDate.setTabIndex(6);
		this.lblExpAmount.setValue(StringResourceUtils.optLocalizeString("{$lblExpAmount.value}", this));
		this.txtExpAmount.setTabIndex(7);
		this.lblExpText.setValue(StringResourceUtils.optLocalizeString("{$lblExpText.value}", this));
		this.txtExpText.setTabIndex(8);
		this.lblProject.setValue(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setTabIndex(9);
		this.cmbProject.setContainerDataSource(Project.class, DAOs.get(ProjectDAO.class).findAll());
		this.cmbProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.lblVat.setValue(StringResourceUtils.optLocalizeString("{$lblVat.value}", this));
		this.cmbVat.setTabIndex(10);
		this.cmbVat.setContainerDataSource(Vat.class, DAOs.get(VatDAO.class).findAll());
		this.cmbVat.setItemCaptionPropertyId(Vat_.vatName.getName());
		this.lblExpAccount.setValue(StringResourceUtils.optLocalizeString("{$lblExpAccount.value}", this));
		this.comboBoxAccount.setTabIndex(3);
		this.comboBoxAccount.setItemCaptionFromAnnotation(false);
		this.comboBoxAccount.setContainerDataSource(LovAccount.class, DAOs.get(LovAccountDAO.class).findAllMine());
		this.comboBoxAccount.setItemCaptionPropertyId("name");
		this.lblExpFlagCostAccount.setValue(StringResourceUtils.optLocalizeString("{$lblExpFlagCostAccount.value}", this));
		this.chkExpFlagCostAccount.setCaption("");
		this.chkExpFlagCostAccount.setTabIndex(11);
		this.lblExpFlagGeneric.setValue(StringResourceUtils.optLocalizeString("{$lblExpFlagGeneric.value}", this));
		this.comboBoxGeneric.setTabIndex(4);
		this.lblExpUnit.setValue(StringResourceUtils.optLocalizeString("{$lblExpUnit.value}", this));
		this.comboBoxUnit.setTabIndex(5);
		this.lblExpQuantity.setValue(StringResourceUtils.optLocalizeString("{$lblExpQuantity.value}", this));
		this.txtExpQuantity.setTabIndex(12);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setTabIndex(14);
		this.cmdReset.setIcon(FontAwesome.REMOVE);
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setTabIndex(13);
		this.cmdDefault1.setIcon(FontAwesome.BOOKMARK);
		this.cmdDefault1.setCaption("Def 1");
		this.cmdDefault1.setClickShortcut(ShortcutAction.KeyCode.NUM1, ShortcutAction.ModifierKey.CTRL);
		this.fieldGroup.bind(this.dateExpDate, Expense_.expDate.getName());
		this.fieldGroup.bind(this.txtExpAmount, Expense_.expAmount.getName());
		this.fieldGroup.bind(this.cmbProject, Expense_.project.getName());
		this.fieldGroup.bind(this.cmbVat, Expense_.vat.getName());
		this.fieldGroup.bind(this.chkExpFlagCostAccount, Expense_.expFlagCostAccount.getName());
		this.fieldGroup.bind(this.comboBoxGeneric, Expense_.expFlagGeneric.getName());
		this.fieldGroup.bind(this.txtExpText, Expense_.expText.getName());
		this.fieldGroup.bind(this.comboBoxUnit, Expense_.expUnit.getName());
		this.fieldGroup.bind(this.txtExpQuantity, Expense_.expQuantity.getName());

		this.menuBarLeftTop.setWidth(100, Unit.PERCENTAGE);
		this.menuBarLeftTop.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutTitle.addComponent(this.menuBarLeftTop);
		this.horizontalLayoutTitle.setComponentAlignment(this.menuBarLeftTop, Alignment.MIDDLE_LEFT);
		this.horizontalLayoutTitle.setExpandRatio(this.menuBarLeftTop, 10.0F);
		this.label.setSizeUndefined();
		this.horizontalLayoutTitle.addComponent(this.label);
		this.horizontalLayoutTitle.setComponentAlignment(this.label, Alignment.MIDDLE_CENTER);
		this.horizontalLayoutTitle.setExpandRatio(this.label, 10.0F);
		this.cmdBack.setSizeUndefined();
		this.horizontalLayoutTitle.addComponent(this.cmdBack);
		this.horizontalLayoutTitle.setComponentAlignment(this.cmdBack, Alignment.MIDDLE_RIGHT);
		this.horizontalLayoutTitle.setExpandRatio(this.cmdBack, 10.0F);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.cmdDefault1.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdDefault1);
		this.horizontalLayout.setComponentAlignment(this.cmdDefault1, Alignment.MIDDLE_CENTER);
		this.form.setColumns(2);
		this.form.setRows(12);
		this.lblExpDate.setSizeUndefined();
		this.form.addComponent(this.lblExpDate, 0, 0);
		this.dateExpDate.setSizeUndefined();
		this.form.addComponent(this.dateExpDate, 1, 0);
		this.lblExpAmount.setSizeUndefined();
		this.form.addComponent(this.lblExpAmount, 0, 1);
		this.txtExpAmount.setSizeUndefined();
		this.form.addComponent(this.txtExpAmount, 1, 1);
		this.lblExpText.setSizeUndefined();
		this.form.addComponent(this.lblExpText, 0, 2);
		this.txtExpText.setWidth(100, Unit.PERCENTAGE);
		this.txtExpText.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtExpText, 1, 2);
		this.lblProject.setSizeUndefined();
		this.form.addComponent(this.lblProject, 0, 3);
		this.cmbProject.setSizeUndefined();
		this.form.addComponent(this.cmbProject, 1, 3);
		this.lblVat.setSizeUndefined();
		this.form.addComponent(this.lblVat, 0, 4);
		this.cmbVat.setSizeUndefined();
		this.form.addComponent(this.cmbVat, 1, 4);
		this.lblExpAccount.setSizeUndefined();
		this.form.addComponent(this.lblExpAccount, 0, 5);
		this.comboBoxAccount.setSizeUndefined();
		this.form.addComponent(this.comboBoxAccount, 1, 5);
		this.lblExpFlagCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblExpFlagCostAccount, 0, 6);
		this.chkExpFlagCostAccount.setWidth(100, Unit.PERCENTAGE);
		this.chkExpFlagCostAccount.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.chkExpFlagCostAccount, 1, 6);
		this.lblExpFlagGeneric.setSizeUndefined();
		this.form.addComponent(this.lblExpFlagGeneric, 0, 7);
		this.comboBoxGeneric.setSizeUndefined();
		this.form.addComponent(this.comboBoxGeneric, 1, 7);
		this.lblExpUnit.setSizeUndefined();
		this.form.addComponent(this.lblExpUnit, 0, 8);
		this.comboBoxUnit.setSizeUndefined();
		this.form.addComponent(this.comboBoxUnit, 1, 8);
		this.lblExpQuantity.setSizeUndefined();
		this.form.addComponent(this.lblExpQuantity, 0, 9);
		this.txtExpQuantity.setSizeUndefined();
		this.form.addComponent(this.txtExpQuantity, 1, 9);
		this.horizontalLayout.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout, 0, 10, 1, 10);
		this.form.setColumnExpandRatio(1, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 11, 1, 11);
		this.form.setRowExpandRatio(11, 1.0F);
		this.form.setSizeFull();
		this.panel.setContent(this.form);
		this.horizontalLayoutTitle.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutTitle.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayoutTitle);
		this.panel.setSizeFull();
		this.verticalLayout.addComponent(this.panel);
		this.verticalLayout.setComponentAlignment(this.panel, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.panel, 10.0F);
		this.verticalLayout.setSizeFull();
		this.setContent(this.verticalLayout);
		this.setSizeFull();

		this.mnuUpload.setCommand(selectedItem -> this.mnuUpload_menuSelected(selectedItem));
		this.mnuTemplate1.setCommand(selectedItem -> this.mnuTemplate1_menuSelected(selectedItem));
		this.mnuTemplate2.setCommand(selectedItem -> this.mnuTemplate2_menuSelected(selectedItem));
		this.mnuTemplate3.setCommand(selectedItem -> this.mnuTemplate3_menuSelected(selectedItem));
		this.mnuTemplate4.setCommand(selectedItem -> this.mnuTemplate4_menuSelected(selectedItem));
		this.mnuTemplate5.setCommand(selectedItem -> this.mnuTemplate5_menuSelected(selectedItem));
		this.mnuTemplate6.setCommand(selectedItem -> this.mnuTemplate6_menuSelected(selectedItem));
		this.mnuTemplate7.setCommand(selectedItem -> this.mnuTemplate7_menuSelected(selectedItem));
		this.mnuTemplate8.setCommand(selectedItem -> this.mnuTemplate8_menuSelected(selectedItem));
		this.mnuTemplate9.setCommand(selectedItem -> this.mnuTemplate9_menuSelected(selectedItem));
		this.mnuTemplate10.setCommand(selectedItem -> this.mnuTemplate10_menuSelected(selectedItem));
		this.menuText.setCommand(selectedItem -> this.menuText_menuSelected(selectedItem));
		this.mnuResetItem.setCommand(selectedItem -> this.mnuResetItem_menuSelected(selectedItem));
		this.mnuDeleteItem.setCommand(selectedItem -> this.mnuDeleteItem_menuSelected(selectedItem));
		this.cmdBack.addClickListener(event -> this.cmdBack_buttonClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
		this.cmdDefault1.addClickListener(event -> this.cmdDefault1_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel label, lblExpDate, lblExpAmount, lblExpText, lblProject, lblVat, lblExpAccount, lblExpFlagCostAccount,
			lblExpFlagGeneric, lblExpUnit, lblExpQuantity;
	private XdevButton cmdBack, cmdSave, cmdReset, cmdDefault1;
	private XdevMenuBar menuBarLeftTop;
	private XdevMenuItem mnuOption, mnuUpload, mnuSeperator2, mnuDefaults, mnuTemplate1, mnuTemplate2, mnuTemplate3,
			mnuTemplate4, mnuTemplate5, mnuTemplate6, mnuTemplate7, mnuTemplate8, mnuTemplate9, mnuTemplate10, menuText,
			mnuSeperator, mnuResetItem, mnuDeleteItem;
	private XdevFieldGroup<Expense> fieldGroup;
	private XdevPanel panel;
	private XdevGridLayout form;
	private XdevComboBox<Project> cmbProject;
	private XdevHorizontalLayout horizontalLayoutTitle, horizontalLayout;
	private XdevComboBox<Vat> cmbVat;
	private XdevPopupDateField dateExpDate;
	private XdevComboBox<?> comboBoxGeneric, comboBoxUnit;
	private XdevCheckBox chkExpFlagCostAccount;
	private XdevTextField txtExpAmount, txtExpText, txtExpQuantity;
	private XdevVerticalLayout verticalLayout;
	private XdevComboBox<LovAccount> comboBoxAccount;
	// </generated-code>

}
