package ch.xwr.seicentobilling.ui.desktop.crm;

import java.util.Date;

import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.res.ApplicationResource;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.dal.CustomerLinkDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.CustomerLink;
import ch.xwr.seicentobilling.entities.CustomerLink_;

public class CustomerLinkPopup extends XdevView {

	/**
	 *
	 */
	public CustomerLinkPopup() {
		super();
		this.initUI();

		// State
		this.comboBoxState.addItems((Object[]) LovState.State.values());
		this.comboBoxType.addItems((Object[]) LovCrm.LinkType.values());
		this.comboBoxDepartment.addItems((Object[]) LovCrm.Department.values());

		// this.comboBoxAccount.addItems((Object[])LovState.Accounts.values());
		// loadDummyCb();

		// get Parameter
		final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId = (Long) UI.getCurrent().getSession().getAttribute("objId");
		CustomerLink bean = null;
		Customer obj = null;

		if (beanId == null) {

			CostAccount beanCsa = Seicento.getLoggedInCostAccount();
			if (beanCsa == null) {
				beanCsa = new CostAccountDAO().findAll().get(0);	//Dev Mode
			}
			final CustomerDAO cusDao = new CustomerDAO();
			obj = cusDao.find(objId);


			bean = new CustomerLink();
			bean.setCnkState(LovState.State.active);
			bean.setCustomer(obj);
			//act.setCostAccount(beanCsa);
			bean.setCnkValidFrom(new Date());
			bean.setCnkType(LovCrm.LinkType.mail);
			bean.setCnkDepartment(LovCrm.Department.main);


		} else {
			final CustomerLinkDAO dao = new CustomerLinkDAO();
			bean = dao.find(beanId.longValue());
		}

		setBeanGui(bean);

	}

	private void setBeanGui(final CustomerLink bean) {
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
		win.setHeight("550");
		win.center();
		win.setModal(true);
		win.setContent(new CustomerLinkPopup());

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
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getCnkId(),
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

	/**
	 * Event handler delegate method for the {@link XdevComboBox}
	 * {@link #comboBoxType}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void comboBoxType_valueChange(final Property.ValueChangeEvent event) {
		if (!this.fieldGroup.isModified()) {
			return;
		}

		String lblText = "Link";
		final LovCrm.LinkType value = (LovCrm.LinkType) event.getProperty().getValue();

		if (value != null) {
			if (value.equals(LovCrm.LinkType.mail)) {
				lblText = "Mail";
			} else if (value.equals(LovCrm.LinkType.phone)) {
				lblText = "Telefon";
		    	this.lblCnkLink.setValue("Telefon");
			} else if (value.equals(LovCrm.LinkType.web)) {
				lblText = "Url";
			}
		}
    	this.lblCnkLink.setValue(lblText);
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.form = new XdevGridLayout();
		this.comboBoxType = new XdevComboBox<>();
		this.comboBoxState = new XdevComboBox<>();
		this.lblCnkIndex = new XdevLabel();
		this.txtCnkIndex = new XdevTextField();
		this.lblCnkType = new XdevLabel();
		this.lblCnkDepartment = new XdevLabel();
		this.comboBoxDepartment = new XdevComboBox<>();
		this.lblCnkValidFrom = new XdevLabel();
		this.dateCnkValidFrom = new XdevPopupDateField();
		this.lblCnkLink = new XdevLabel();
		this.txtCnkLink = new XdevTextField();
		this.lblCnkRemark = new XdevLabel();
		this.txtCnkRemark = new XdevTextField();
		this.lblCnkState = new XdevLabel();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(CustomerLink.class);

		this.setCaption("CustomerLink");
		this.panel.setCaption("Kontaktkoordinaten");
		this.panel.setTabIndex(0);
		this.form.setCaption("CustomerLink");
		this.lblCnkIndex.setValue("Index");
		this.lblCnkType.setValue("Type");
		this.lblCnkDepartment.setValue("Bereich");
		this.lblCnkValidFrom.setValue("ValidFrom");
		this.lblCnkLink.setValue("Link");
		this.lblCnkRemark.setValue("Remark");
		this.lblCnkState.setValue("State");
		this.horizontalLayout.setMargin(new MarginInfo(true, false, true, false));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption("Speichern");
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption("Abbrechen");
		this.cmdReset.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.fieldGroup.bind(this.txtCnkIndex, CustomerLink_.cnkIndex.getName());
		this.fieldGroup.bind(this.comboBoxType, CustomerLink_.cnkType.getName());
		this.fieldGroup.bind(this.dateCnkValidFrom, CustomerLink_.cnkValidFrom.getName());
		this.fieldGroup.bind(this.txtCnkLink, CustomerLink_.cnkLink.getName());
		this.fieldGroup.bind(this.txtCnkRemark, CustomerLink_.cnkRemark.getName());
		this.fieldGroup.bind(this.comboBoxState, CustomerLink_.cnkState.getName());
		this.fieldGroup.bind(this.comboBoxDepartment, CustomerLink_.cnkDepartment.getName());

		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.form.setColumns(2);
		this.form.setRows(9);
		this.comboBoxType.setSizeUndefined();
		this.form.addComponent(this.comboBoxType, 1, 1);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 6);
		this.lblCnkIndex.setSizeUndefined();
		this.form.addComponent(this.lblCnkIndex, 0, 0);
		this.txtCnkIndex.setSizeUndefined();
		this.form.addComponent(this.txtCnkIndex, 1, 0);
		this.lblCnkType.setSizeUndefined();
		this.form.addComponent(this.lblCnkType, 0, 1);
		this.lblCnkDepartment.setSizeUndefined();
		this.form.addComponent(this.lblCnkDepartment, 0, 2);
		this.comboBoxDepartment.setSizeUndefined();
		this.form.addComponent(this.comboBoxDepartment, 1, 2);
		this.lblCnkValidFrom.setSizeUndefined();
		this.form.addComponent(this.lblCnkValidFrom, 0, 3);
		this.dateCnkValidFrom.setSizeUndefined();
		this.form.addComponent(this.dateCnkValidFrom, 1, 3);
		this.lblCnkLink.setSizeUndefined();
		this.form.addComponent(this.lblCnkLink, 0, 4);
		this.txtCnkLink.setWidth(100, Unit.PERCENTAGE);
		this.txtCnkLink.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtCnkLink, 1, 4);
		this.lblCnkRemark.setSizeUndefined();
		this.form.addComponent(this.lblCnkRemark, 0, 5);
		this.txtCnkRemark.setWidth(100, Unit.PERCENTAGE);
		this.txtCnkRemark.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtCnkRemark, 1, 5);
		this.lblCnkState.setSizeUndefined();
		this.form.addComponent(this.lblCnkState, 0, 6);
		this.horizontalLayout.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout, 1, 7);
		this.form.setColumnExpandRatio(1, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 8, 1, 8);
		this.form.setRowExpandRatio(8, 1.0F);
		this.form.setSizeFull();
		this.panel.setContent(this.form);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setSizeFull();

		this.comboBoxType.addValueChangeListener(event -> this.comboBoxType_valueChange(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel lblCnkIndex, lblCnkType, lblCnkDepartment, lblCnkValidFrom, lblCnkLink, lblCnkRemark, lblCnkState;
	private XdevButton cmdSave, cmdReset;
	private XdevHorizontalLayout horizontalLayout;
	private XdevPopupDateField dateCnkValidFrom;
	private XdevComboBox<?> comboBoxType, comboBoxState, comboBoxDepartment;
	private XdevPanel panel;
	private XdevGridLayout form;
	private XdevTextField txtCnkIndex, txtCnkLink, txtCnkRemark;
	private XdevFieldGroup<CustomerLink> fieldGroup;
	// </generated-code>

}
