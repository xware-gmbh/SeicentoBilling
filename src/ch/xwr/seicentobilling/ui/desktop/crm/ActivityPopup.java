package ch.xwr.seicentobilling.ui.desktop.crm;

import java.util.Date;

import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.dal.DAOs;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTextArea;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.ActivityDAO;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.entities.Activity;
import ch.xwr.seicentobilling.entities.Activity_;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;
import ch.xwr.seicentobilling.entities.Customer;

public class ActivityPopup extends XdevView {

	/**
	 *
	 */
	public ActivityPopup() {
		super();
		this.initUI();

		// State
		this.comboBoxState.addItems((Object[]) LovState.State.values());
		this.comboBoxType.addItems((Object[]) LovCrm.ActivityType.values());

		// this.comboBoxAccount.addItems((Object[])LovState.Accounts.values());
		// loadDummyCb();

		// get Parameter
		final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId = (Long) UI.getCurrent().getSession().getAttribute("objId");
		Activity bean = null;
		Customer obj = null;

		if (beanId == null) {

			CostAccount beanCsa = Seicento.getLoggedInCostAccount();
			if (beanCsa == null) {
				beanCsa = new CostAccountDAO().findAll().get(0);	//Dev Mode
			}
			final CustomerDAO cusDao = new CustomerDAO();
			obj = cusDao.find(objId);


			bean = new Activity();
			bean.setActState(LovState.State.active);
			bean.setCustomer(obj);
			bean.setCostAccount(beanCsa);
			bean.setActDate(new Date());
			bean.setActType(LovCrm.ActivityType.misc);
			bean.setActText("");


		} else {
			final ActivityDAO dao = new ActivityDAO();
			bean = dao.find(beanId.longValue());
		}

		setBeanGui(bean);

	}

	private void setBeanGui(final Activity bean) {
		// set Bean + Fields
		this.fieldGroup.setItemDataSource(bean);

		// set RO Fields
		setROFields();

		//postLoadAccountAction(bean);
		//this.txtExpText.focus();
	}


	private void setROFields() {
//		this.dateExpBooked.setEnabled(false);
//		this.cmbPeriode.setEnabled(false);
	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		win.setWidth("720");
		win.setHeight("600");
		win.center();
		win.setModal(true);
		win.setContent(new ActivityPopup());

		return win;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");

		try {
			this.fieldGroup.save();
			final RowObjectManager man = new RowObjectManager();
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getactId(),
					this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

			((Window) this.getParent()).close();
			Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);
		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}

	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReset}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_buttonClick(final Button.ClickEvent event) {
		this.fieldGroup.discard();
		((Window) this.getParent()).close();
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.form = new XdevGridLayout();
		this.lblActDate = new XdevLabel();
		this.dateActDate = new XdevPopupDateField();
		this.lblActType = new XdevLabel();
		this.comboBoxType = new XdevComboBox<>();
		this.lblActText = new XdevLabel();
		this.textArea = new XdevTextArea();
		this.lblActLink = new XdevLabel();
		this.txtActLink = new XdevTextField();
		this.lblActFollowingUpDate = new XdevLabel();
		this.lblCostAccount = new XdevLabel();
		this.cmbCostAccount = new XdevComboBox<>();
		this.lblActState = new XdevLabel();
		this.comboBoxState = new XdevComboBox<>();
		this.dateActFollowingUpDate = new XdevPopupDateField();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Activity.class);

		this.setCaption("Aktivität");
		this.panel.setCaption("Aktivität");
		this.panel.setTabIndex(0);
		this.lblActDate.setValue("Datum");
		this.lblActType.setValue("Type");
		this.lblActText.setValue("Text");
		this.textArea.setRows(5);
		this.lblActLink.setValue("Link");
		this.lblActFollowingUpDate.setValue("Folgetermin");
		this.lblCostAccount.setValue("Mitarbeiter");
		this.cmbCostAccount.setContainerDataSource(CostAccount.class, DAOs.get(CostAccountDAO.class).findAll());
		this.cmbCostAccount.setItemCaptionPropertyId(CostAccount_.csaCode.getName());
		this.lblActState.setValue("Status");
		this.horizontalLayout.setMargin(new MarginInfo(true, false, false, true));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption("Speichern");
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdReset.setIcon(FontAwesome.CLOSE);
		this.cmdReset.setCaption("Abbrechen");
		this.cmdReset.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.fieldGroup.bind(this.dateActDate, Activity_.actDate.getName());
		this.fieldGroup.bind(this.comboBoxType, Activity_.actType.getName());
		this.fieldGroup.bind(this.textArea, Activity_.actText.getName());
		this.fieldGroup.bind(this.txtActLink, Activity_.actLink.getName());
		this.fieldGroup.bind(this.dateActFollowingUpDate, Activity_.actFollowingUpDate.getName());
		this.fieldGroup.bind(this.cmbCostAccount, Activity_.costAccount.getName());
		this.fieldGroup.bind(this.comboBoxState, Activity_.actState.getName());

		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.form.setColumns(2);
		this.form.setRows(9);
		this.lblActDate.setSizeUndefined();
		this.form.addComponent(this.lblActDate, 0, 0);
		this.dateActDate.setSizeUndefined();
		this.form.addComponent(this.dateActDate, 1, 0);
		this.lblActType.setSizeUndefined();
		this.form.addComponent(this.lblActType, 0, 1);
		this.comboBoxType.setSizeUndefined();
		this.form.addComponent(this.comboBoxType, 1, 1);
		this.lblActText.setSizeUndefined();
		this.form.addComponent(this.lblActText, 0, 2);
		this.textArea.setWidth(100, Unit.PERCENTAGE);
		this.textArea.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.textArea, 1, 2);
		this.lblActLink.setSizeUndefined();
		this.form.addComponent(this.lblActLink, 0, 3);
		this.txtActLink.setWidth(100, Unit.PERCENTAGE);
		this.txtActLink.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtActLink, 1, 3);
		this.lblActFollowingUpDate.setSizeUndefined();
		this.form.addComponent(this.lblActFollowingUpDate, 0, 4);
		this.lblCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblCostAccount, 0, 5);
		this.cmbCostAccount.setSizeUndefined();
		this.form.addComponent(this.cmbCostAccount, 1, 5);
		this.lblActState.setSizeUndefined();
		this.form.addComponent(this.lblActState, 0, 6);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 6);
		this.dateActFollowingUpDate.setSizeUndefined();
		this.form.addComponent(this.dateActFollowingUpDate, 1, 4);
		this.horizontalLayout.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout, 1, 7);
		this.form.setColumnExpandRatio(1, 10.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 8, 1, 8);
		this.form.setRowExpandRatio(8, 1.0F);
		this.form.setSizeFull();
		this.panel.setContent(this.form);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setSizeFull();

		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel lblActDate, lblActType, lblActText, lblActLink, lblActFollowingUpDate, lblCostAccount, lblActState;
	private XdevButton cmdSave, cmdReset;
	private XdevComboBox<CostAccount> cmbCostAccount;
	private XdevHorizontalLayout horizontalLayout;
	private XdevPopupDateField dateActDate, dateActFollowingUpDate;
	private XdevTextArea textArea;
	private XdevComboBox<?> comboBoxType, comboBoxState;
	private XdevPanel panel;
	private XdevGridLayout form;
	private XdevTextField txtActLink;
	private XdevFieldGroup<Activity> fieldGroup;
	// </generated-code>

}
