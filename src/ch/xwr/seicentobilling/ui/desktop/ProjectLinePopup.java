package ch.xwr.seicentobilling.ui.desktop;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;

import com.vaadin.data.Property;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.res.ApplicationResource;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
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
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.helper.ProjectLineHelper;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.ProjectLineDAO;
import ch.xwr.seicentobilling.dal.ProjectLineTemplateDAO;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Periode_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectLine;
import ch.xwr.seicentobilling.entities.ProjectLineTemplate;
import ch.xwr.seicentobilling.entities.ProjectLine_;
import ch.xwr.seicentobilling.entities.Project_;
import ch.xwr.seicentobilling.ui.desktop.project.ProjectLookupPopup;
import ch.xwr.seicentobilling.ui.phone.TextListPopup;

public class ProjectLinePopup extends XdevView {

	/**
	 *
	 */
	public ProjectLinePopup() {
		super();
		this.initUI();

		// State
		this.comboBoxState.addItems((Object[]) LovState.State.values());
		this.comboBoxWorktype.addItems((Object[]) LovState.WorkType.values());
		this.cmbProject.addItem(new ProjectDAO().findAllActive());

		// get Parameter
		final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId"); // projectline
		final Long objId = (Long) UI.getCurrent().getSession().getAttribute("objId"); // Periode
		ProjectLine bean = null;
		Periode obj = null;

		if (beanId == null) {
			// new
			final PeriodeDAO objDao = new PeriodeDAO();
			obj = objDao.find(objId);

			bean = new ProjectLine();
			bean.setPrlState(LovState.State.active);
			bean.setPrlWorkType(LovState.WorkType.project);
			bean.setPrlReportDate(new Date());
			bean.setPeriode(obj);

		} else {
			final ProjectLineDAO dao = new ProjectLineDAO();
			bean = dao.find(beanId.longValue());

			prepareProjectCombo(bean.getProject());
		}

		setBeanGui(bean);
		checkTemplates();
	}

	private void setBeanGui(final ProjectLine bean) {
		// set Bean + Fields
		this.fieldGroup.setItemDataSource(bean);
		//this.fieldGroupProject.setItemDataSource(bean.getProject());
		//this.lookupField.setCon

		setROFields();

		// focus
		this.datePrlReportDate.focus();
	}

	private void setROFields() {
		// Readonly
		this.cmbPeriode.setEnabled(false);
		this.cmbProject.setEnabled(false);

	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		win.setWidth("820");
		win.setHeight("480");
		win.center();
		win.setModal(true);
		win.setContent(new ProjectLinePopup());

		return win;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		if (!areFieldsValid()) {
			return;
		}

		UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");
		try {
			this.fieldGroup.save();

			final RowObjectManager man = new RowObjectManager();
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getPrlId(),
					this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

			((Window) this.getParent()).close();
			Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);

		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	private boolean areFieldsValid() {
		if (this.fieldGroup.isValid()) {
			return true;
		}
		AbstractField<T> fld = null;
		try {
			final Collection<?> flds = this.fieldGroup.getFields();
			for (final Iterator<?> iterator = flds.iterator(); iterator.hasNext();) {
				fld = (AbstractField<T>) iterator.next();
				if (!fld.isValid()) {
					fld.focus();
					fld.validate();
				}
			}

		} catch (final Exception e) {
			final Object prop = this.fieldGroup.getPropertyId(fld);
			Notification.show("Feld ist ungültig", prop.toString(), Notification.Type.ERROR_MESSAGE);
		}

		return false;
	}

	private void loadTemplate(final int iKey) {
		final ProjectLine line = this.fieldGroup.getItemDataSource().getBean();

		final ProjectLineTemplateDAO dao = new ProjectLineTemplateDAO();
		final ProjectLineTemplate tpl = dao.findByKeyNumber(line.getPeriode().getCostAccount(), iKey);

		if (tpl == null) {
			return; // not found
		}

		line.setPrlHours(tpl.getPrtHours());
		line.setPrlRate(tpl.getPrtRate());
		line.setPrlText(tpl.getprtText());
		line.setPrlWorkType(tpl.getprtWorkType());
		line.setProject(tpl.getProject());
		line.setPrlState(tpl.getPrtState());

		prepareProjectCombo(tpl.getProject());
		this.fieldGroup.setItemDataSource(line);

		setROFields();
	}

	private void popupProjectLookup() {
		final Window win = ProjectLookupPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");

				if (beanId != null && beanId > 0) {
					final Project bean = new ProjectDAO().find(beanId);
					prepareProjectCombo(bean);

					//nur setzene bei neuem Record
					if (ProjectLinePopup.this.fieldGroup.getItemDataSource().getBean().getPrlId() == null) {
						ProjectLinePopup.this.txtPrlRate.setValue("" + bean.getProRate());
					}

					//ProjectLinePopup.this.fieldGroupProject.setItemDataSource(bean);
				}
			}
		});
		this.getUI().addWindow(win);

	}

	private void prepareProjectCombo(final Project bean) {
		ProjectLinePopup.this.cmbProject.addItem(bean);
		ProjectLinePopup.this.cmbProject.setValue(bean);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdCancel}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCancel_buttonClick(final Button.ClickEvent event) {
		this.fieldGroup.discard();
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #btnSearch}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnSearch_buttonClick(final Button.ClickEvent event) {
		popupProjectLookup();
	}

	/**
	 * Event handler delegate method for the {@link XdevPopupDateField}
	 * {@link #datePrlReportDateTo}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void datePrlReportDateTo_valueChange(final Property.ValueChangeEvent event) {
		validateTimeFromTo();
		//if (!this.datePrlReportDateTo.isModified()) {
			calcDurationFromTime();
		//}
	}

	private void validateTimeFromTo() {
		Date d1 = this.datePrlReportDate.getValue();
		Date dateFrom = this.datePrlReportDateFrom.getValue();
		Date dateTo = this.datePrlReportDateTo.getValue();
		final ProjectLineHelper hlp = new ProjectLineHelper();

		if (d1 == null) {
			d1 = new Date();
		}

		if (dateFrom != null) {
			dateFrom = hlp.getDateCorrect(d1, dateFrom);
			this.datePrlReportDateFrom.setValue(dateFrom);
		}
		if (dateTo != null) {
			dateTo = hlp.getDateCorrect(d1, dateTo);
			if (dateFrom != null && dateTo.before(dateFrom)) {
				dateTo = dateFrom;
			}
			this.datePrlReportDateTo.setValue(dateTo);
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevPopupDateField}
	 * {@link #datePrlReportDateFrom}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void datePrlReportDateFrom_valueChange(final Property.ValueChangeEvent event) {
		validateTimeFromTo();
		//if (!this.datePrlReportDateFrom.isModified()) {
			calcDurationFromTime();
		//}
	}

	private void calcDurationFromTime() {
		final Date fromHH = this.datePrlReportDateFrom.getValue();
		final Date toHH = this.datePrlReportDateTo.getValue();

		if (fromHH == null || toHH == null) {
			return;
		}

		final ProjectLineHelper hlp = new ProjectLineHelper();
		final double hours = hlp.calcDurationFromTime(fromHH, toHH);

		this.txtPrlHours.setValue(new DecimalFormat("####.##").format(hours));
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdStartStop}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdStartStop_buttonClick(final Button.ClickEvent event) {
		handleStartStop();

	}

	private void handleStartStop() {
		final Date d1 = this.datePrlReportDate.getValue();
		if (d1 == null) {
			return;
		}

		final ProjectLineHelper hlp = new ProjectLineHelper();
		final Date retDate = hlp.getStartStopTime(d1);

		if (this.datePrlReportDateFrom.getValue() == null ) {
			this.datePrlReportDateFrom.setValue(retDate);
		} else {
			if (this.datePrlReportDateTo.getValue() == null ) {
				this.datePrlReportDateTo.setValue(retDate);
			}
		}
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
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuStartStop}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuStartStop_menuSelected(final MenuBar.MenuItem selectedItem) {
		handleStartStop();
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
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuSaveItem}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuSaveItem_menuSelected(final MenuBar.MenuItem selectedItem) {
		cmdSave_buttonClick(null);
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

	private void checkTemplates() {
		final ProjectLine line = this.fieldGroup.getItemDataSource().getBean();

		final ProjectLineTemplateDAO dao = new ProjectLineTemplateDAO();
		final List<ProjectLineTemplate> lst = dao.findByCostAccount(line.getPeriode().getCostAccount());

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

		for (final Iterator<ProjectLineTemplate> iterator = lst.iterator(); iterator.hasNext();) {
			final ProjectLineTemplate tpl = iterator.next();
			final int nbr = tpl.getPrtKeyNumber();
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
		UI.getCurrent().getSession().setAttribute("target", 2);

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
					ProjectLinePopup.this.txtPrlText.setValue(reason);
				}

			}
		});

		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmd2}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmd2_buttonClick(final Button.ClickEvent event) {
		loadTemplate(2);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmd3}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmd3_buttonClick(final Button.ClickEvent event) {
		loadTemplate(3);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmd4}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmd4_buttonClick(final Button.ClickEvent event) {
		loadTemplate(4);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmd5}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmd5_buttonClick(final Button.ClickEvent event) {
		loadTemplate(5);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmd6}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmd6_buttonClick(final Button.ClickEvent event) {
		loadTemplate(6);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmd7}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmd7_buttonClick(final Button.ClickEvent event) {
		loadTemplate(7);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmd8}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmd8_buttonClick(final Button.ClickEvent event) {
		loadTemplate(8);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmd9}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmd9_buttonClick(final Button.ClickEvent event) {
		loadTemplate(9);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmd10}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmd10_buttonClick(final Button.ClickEvent event) {
		loadTemplate(0);

	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalLayout = new XdevVerticalLayout();
		this.horizontalLayout3 = new XdevHorizontalLayout();
		this.menuBar = new XdevMenuBar();
		this.menuOption = this.menuBar.addItem("Optionen", null);
		this.mnuStartStop = this.menuOption.addItem("Start/Stop", null);
		this.mnuSeperator2 = this.menuOption.addSeparator();
		this.mnuDefaults = this.menuOption.addItem("Vorlage", null);
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
		this.menuText = this.menuOption.addItem("Text...", null);
		this.mnuSeperator = this.menuOption.addSeparator();
		this.mnuResetItem = this.menuOption.addItem("Abbrechen", null);
		this.mnuSaveItem = this.menuOption.addItem("Speichern", null);
		this.label = new XdevLabel();
		this.label3 = new XdevLabel();
		this.panel = new XdevPanel();
		this.form = new XdevGridLayout();
		this.lblPeriode = new XdevLabel();
		this.cmbPeriode = new XdevComboBox<>();
		this.lblPrlReportDate = new XdevLabel();
		this.datePrlReportDate = new XdevPopupDateField();
		this.lblPrlFromTo = new XdevLabel();
		this.datePrlReportDateFrom = new XdevPopupDateField();
		this.lblProject = new XdevLabel();
		this.cmbProject = new XdevComboBox<>();
		this.btnSearch = new XdevButton();
		this.lblPrlHours = new XdevLabel();
		this.txtPrlHours = new XdevTextField();
		this.lblPrlRate = new XdevLabel();
		this.txtPrlRate = new XdevTextField();
		this.lblPrlText = new XdevLabel();
		this.txtPrlText = new XdevTextField();
		this.lblPrlWorkType = new XdevLabel();
		this.comboBoxWorktype = new XdevComboBox<>();
		this.lblPrlState = new XdevLabel();
		this.comboBoxState = new XdevComboBox<>();
		this.datePrlReportDateTo = new XdevPopupDateField();
		this.fieldGroup = new XdevFieldGroup<>(ProjectLine.class);
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdCancel = new XdevButton();
		this.label2 = new XdevLabel();
		this.cmdStartStop = new XdevButton();
		this.cmdDefault1 = new XdevButton();
		this.horizontalLayoutShortcut = new XdevHorizontalLayout();
		this.cmd2 = new XdevButton();
		this.cmd3 = new XdevButton();
		this.cmd4 = new XdevButton();
		this.cmd5 = new XdevButton();
		this.cmd6 = new XdevButton();
		this.cmd7 = new XdevButton();
		this.cmd8 = new XdevButton();
		this.cmd9 = new XdevButton();
		this.cmd10 = new XdevButton();

		this.verticalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayout3.setMargin(new MarginInfo(false));
		this.menuOption.setIcon(FontAwesome.NAVICON);
		this.mnuStartStop.setIcon(FontAwesome.CLOCK_O);
		this.mnuDefaults.setIcon(FontAwesome.BOOKMARK);
		this.menuText.setIcon(FontAwesome.LIST_ALT);
		this.mnuResetItem
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.mnuResetItem.setCheckable(true);
		this.mnuSaveItem.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.mnuSaveItem.setCheckable(true);
		this.label.setValue("Rapporte erfassen");
		this.panel.setTabIndex(0);
		this.panel.setStyleName("active");
		this.lblPeriode.setValue(StringResourceUtils.optLocalizeString("{$lblPeriode.value}", this));
		this.cmbPeriode.setContainerDataSource(Periode.class);
		this.cmbPeriode.setItemCaptionPropertyId(Periode_.perName.getName());
		this.lblPrlReportDate.setValue(StringResourceUtils.optLocalizeString("{$lblPrlReportDate.value}", this));
		this.datePrlReportDate.setDateFormat("dd.MM.yyyy");
		this.datePrlReportDate.setRequired(true);
		this.lblPrlFromTo.setValue("Von/Bis");
		this.datePrlReportDateFrom.setDateFormat("HH:mm");
		this.datePrlReportDateFrom.setResolution(Resolution.MINUTE);
		this.lblProject.setValue(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setRequired(true);
		this.cmbProject.setAutoQueryData(false);
		this.cmbProject.setImmediate(false);
		this.cmbProject.setEnabled(false);
		this.cmbProject.setContainerDataSource(Project.class, false);
		this.cmbProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.btnSearch.setIcon(FontAwesome.SEARCH);
		this.btnSearch.setCaption("");
		this.btnSearch.setDescription("Suchen...");
		this.lblPrlHours.setValue(StringResourceUtils.optLocalizeString("{$lblPrlHours.value}", this));
		this.txtPrlHours.setConverter(ConverterBuilder.stringToDouble().build());
		this.txtPrlHours.setRequired(true);
		this.lblPrlRate.setValue(StringResourceUtils.optLocalizeString("{$lblPrlRate.value}", this));
		this.txtPrlRate.setConverter(ConverterBuilder.stringToDouble().build());
		this.txtPrlRate.setRequired(true);
		this.lblPrlText.setValue(StringResourceUtils.optLocalizeString("{$lblPrlText.value}", this));
		this.txtPrlText.setMaxLength(384);
		this.txtPrlText
				.addValidator(new StringLengthValidator("Der Text kann maximall 380 Zeichen lang sein.", null, 380, true));
		this.lblPrlWorkType.setValue(StringResourceUtils.optLocalizeString("{$lblPrlWorkType.value}", this));
		this.lblPrlState.setValue(StringResourceUtils.optLocalizeString("{$lblPrlState.value}", this));
		this.datePrlReportDateTo.setDateFormat("HH:mm");
		this.datePrlReportDateTo.setResolution(Resolution.MINUTE);
		this.fieldGroup.bind(this.cmbPeriode, ProjectLine_.periode.getName());
		this.fieldGroup.bind(this.datePrlReportDate, ProjectLine_.prlReportDate.getName());
		this.fieldGroup.bind(this.txtPrlHours, ProjectLine_.prlHours.getName());
		this.fieldGroup.bind(this.txtPrlText, ProjectLine_.prlText.getName());
		this.fieldGroup.bind(this.txtPrlRate, ProjectLine_.prlRate.getName());
		this.fieldGroup.bind(this.cmbProject, ProjectLine_.project.getName());
		this.fieldGroup.bind(this.comboBoxWorktype, ProjectLine_.prlWorkType.getName());
		this.fieldGroup.bind(this.datePrlReportDateFrom, ProjectLine_.prlTimeFrom.getName());
		this.fieldGroup.bind(this.datePrlReportDateTo, ProjectLine_.prlTimeTo.getName());
		this.fieldGroup.bind(this.comboBoxState, ProjectLine_.prlState.getName());
		this.horizontalLayout.setMargin(new MarginInfo(false, true, false, true));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdCancel.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdCancel.setCaption(StringResourceUtils.optLocalizeString("{$cmdCancel.caption}", this));
		this.cmdCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.label2.setValue("            ");
		this.cmdStartStop.setIcon(FontAwesome.CLOCK_O);
		this.cmdStartStop.setCaption("Start/Stop");
		this.cmdStartStop.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.cmdDefault1.setIcon(FontAwesome.BOOKMARK);
		this.cmdDefault1.setCaption("Def 1");
		this.cmdDefault1.setClickShortcut(ShortcutAction.KeyCode.NUM1, ShortcutAction.ModifierKey.CTRL);
		this.horizontalLayoutShortcut.setSpacing(false);
		this.horizontalLayoutShortcut.setMargin(new MarginInfo(false));
		this.cmd2.setCaption("B2");
		this.cmd2.setStyleName("tray tiny");
		this.cmd2.setClickShortcut(ShortcutAction.KeyCode.NUM2, ShortcutAction.ModifierKey.CTRL);
		this.cmd3.setCaption("B3");
		this.cmd3.setStyleName("tray tiny");
		this.cmd3.setClickShortcut(ShortcutAction.KeyCode.NUM3, ShortcutAction.ModifierKey.CTRL);
		this.cmd4.setCaption("B4");
		this.cmd4.setStyleName("tray tiny");
		this.cmd4.setClickShortcut(ShortcutAction.KeyCode.NUM4, ShortcutAction.ModifierKey.CTRL);
		this.cmd5.setCaption("B5");
		this.cmd5.setStyleName("tray tiny");
		this.cmd5.setClickShortcut(ShortcutAction.KeyCode.NUM5, ShortcutAction.ModifierKey.CTRL);
		this.cmd6.setCaption("B6");
		this.cmd6.setStyleName("tray tiny");
		this.cmd6.setClickShortcut(ShortcutAction.KeyCode.NUM6, ShortcutAction.ModifierKey.CTRL);
		this.cmd7.setCaption("B7");
		this.cmd7.setStyleName("tray tiny");
		this.cmd7.setClickShortcut(ShortcutAction.KeyCode.NUM7, ShortcutAction.ModifierKey.CTRL);
		this.cmd8.setCaption("B8");
		this.cmd8.setStyleName("tray tiny");
		this.cmd8.setClickShortcut(ShortcutAction.KeyCode.NUM8, ShortcutAction.ModifierKey.CTRL);
		this.cmd9.setCaption("B9");
		this.cmd9.setStyleName("tray tiny");
		this.cmd9.setClickShortcut(ShortcutAction.KeyCode.NUM9, ShortcutAction.ModifierKey.CTRL);
		this.cmd10.setCaption("B10");
		this.cmd10.setStyleName("tray tiny");
		this.cmd10.setClickShortcut(ShortcutAction.KeyCode.NUM0, ShortcutAction.ModifierKey.CTRL);

		this.menuBar.setWidth(100, Unit.PERCENTAGE);
		this.menuBar.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout3.addComponent(this.menuBar);
		this.horizontalLayout3.setComponentAlignment(this.menuBar, Alignment.MIDDLE_CENTER);
		this.horizontalLayout3.setExpandRatio(this.menuBar, 30.0F);
		this.label.setSizeUndefined();
		this.horizontalLayout3.addComponent(this.label);
		this.horizontalLayout3.setComponentAlignment(this.label, Alignment.MIDDLE_RIGHT);
		this.horizontalLayout3.setExpandRatio(this.label, 80.0F);
		this.label3.setWidth(60, Unit.PIXELS);
		this.label3.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout3.addComponent(this.label3);
		this.horizontalLayout3.setComponentAlignment(this.label3, Alignment.MIDDLE_CENTER);
		this.form.setColumns(5);
		this.form.setRows(7);
		this.lblPeriode.setSizeUndefined();
		this.form.addComponent(this.lblPeriode, 0, 0);
		this.cmbPeriode.setWidth(100, Unit.PERCENTAGE);
		this.cmbPeriode.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbPeriode, 1, 0);
		this.lblPrlReportDate.setSizeUndefined();
		this.form.addComponent(this.lblPrlReportDate, 0, 1);
		this.datePrlReportDate.setSizeUndefined();
		this.form.addComponent(this.datePrlReportDate, 1, 1);
		this.lblPrlFromTo.setSizeUndefined();
		this.form.addComponent(this.lblPrlFromTo, 2, 1);
		this.datePrlReportDateFrom.setWidth(100, Unit.PIXELS);
		this.datePrlReportDateFrom.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.datePrlReportDateFrom, 3, 1);
		this.lblProject.setSizeUndefined();
		this.form.addComponent(this.lblProject, 0, 2);
		this.cmbProject.setWidth(100, Unit.PERCENTAGE);
		this.cmbProject.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbProject, 1, 2, 2, 2);
		this.btnSearch.setSizeUndefined();
		this.form.addComponent(this.btnSearch, 3, 2);
		this.lblPrlHours.setSizeUndefined();
		this.form.addComponent(this.lblPrlHours, 0, 3);
		this.txtPrlHours.setWidth(150, Unit.PIXELS);
		this.txtPrlHours.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPrlHours, 1, 3);
		this.lblPrlRate.setSizeUndefined();
		this.form.addComponent(this.lblPrlRate, 2, 3);
		this.txtPrlRate.setWidth(150, Unit.PIXELS);
		this.txtPrlRate.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPrlRate, 3, 3, 4, 3);
		this.lblPrlText.setSizeUndefined();
		this.form.addComponent(this.lblPrlText, 0, 4);
		this.txtPrlText.setWidth(100, Unit.PERCENTAGE);
		this.txtPrlText.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPrlText, 1, 4, 3, 4);
		this.lblPrlWorkType.setSizeUndefined();
		this.form.addComponent(this.lblPrlWorkType, 0, 5);
		this.comboBoxWorktype.setSizeUndefined();
		this.form.addComponent(this.comboBoxWorktype, 1, 5);
		this.lblPrlState.setSizeUndefined();
		this.form.addComponent(this.lblPrlState, 2, 5);
		this.comboBoxState.setWidth(100, Unit.PERCENTAGE);
		this.comboBoxState.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.comboBoxState, 3, 5, 4, 5);
		this.datePrlReportDateTo.setWidth(100, Unit.PIXELS);
		this.datePrlReportDateTo.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.datePrlReportDateTo, 4, 1);
		this.form.setColumnExpandRatio(1, 10.0F);
		this.form.setColumnExpandRatio(4, 10.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 6, 4, 6);
		this.form.setRowExpandRatio(6, 1.0F);
		this.form.setSizeFull();
		this.panel.setContent(this.form);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_CENTER);
		this.cmdCancel.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdCancel);
		this.horizontalLayout.setComponentAlignment(this.cmdCancel, Alignment.MIDDLE_CENTER);
		this.label2.setWidth(100, Unit.PIXELS);
		this.label2.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout.addComponent(this.label2);
		this.horizontalLayout.setComponentAlignment(this.label2, Alignment.MIDDLE_CENTER);
		this.cmdStartStop.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdStartStop);
		this.horizontalLayout.setComponentAlignment(this.cmdStartStop, Alignment.MIDDLE_CENTER);
		this.cmdDefault1.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdDefault1);
		this.horizontalLayout.setComponentAlignment(this.cmdDefault1, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayout_spacer = new CustomComponent();
		horizontalLayout_spacer.setSizeFull();
		this.horizontalLayout.addComponent(horizontalLayout_spacer);
		this.horizontalLayout.setExpandRatio(horizontalLayout_spacer, 1.0F);
		this.cmd2.setSizeUndefined();
		this.horizontalLayoutShortcut.addComponent(this.cmd2);
		this.horizontalLayoutShortcut.setComponentAlignment(this.cmd2, Alignment.MIDDLE_CENTER);
		this.cmd3.setSizeUndefined();
		this.horizontalLayoutShortcut.addComponent(this.cmd3);
		this.horizontalLayoutShortcut.setComponentAlignment(this.cmd3, Alignment.MIDDLE_CENTER);
		this.cmd4.setSizeUndefined();
		this.horizontalLayoutShortcut.addComponent(this.cmd4);
		this.horizontalLayoutShortcut.setComponentAlignment(this.cmd4, Alignment.MIDDLE_CENTER);
		this.cmd5.setSizeUndefined();
		this.horizontalLayoutShortcut.addComponent(this.cmd5);
		this.horizontalLayoutShortcut.setComponentAlignment(this.cmd5, Alignment.MIDDLE_CENTER);
		this.cmd6.setSizeUndefined();
		this.horizontalLayoutShortcut.addComponent(this.cmd6);
		this.horizontalLayoutShortcut.setComponentAlignment(this.cmd6, Alignment.MIDDLE_CENTER);
		this.cmd7.setSizeUndefined();
		this.horizontalLayoutShortcut.addComponent(this.cmd7);
		this.horizontalLayoutShortcut.setComponentAlignment(this.cmd7, Alignment.MIDDLE_CENTER);
		this.cmd8.setSizeUndefined();
		this.horizontalLayoutShortcut.addComponent(this.cmd8);
		this.horizontalLayoutShortcut.setComponentAlignment(this.cmd8, Alignment.MIDDLE_CENTER);
		this.cmd9.setSizeUndefined();
		this.horizontalLayoutShortcut.addComponent(this.cmd9);
		this.horizontalLayoutShortcut.setComponentAlignment(this.cmd9, Alignment.MIDDLE_CENTER);
		this.cmd10.setSizeUndefined();
		this.horizontalLayoutShortcut.addComponent(this.cmd10);
		this.horizontalLayoutShortcut.setComponentAlignment(this.cmd10, Alignment.MIDDLE_CENTER);
		this.horizontalLayout3.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout3.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayout3);
		this.panel.setWidth(100, Unit.PERCENTAGE);
		this.panel.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.panel);
		this.verticalLayout.setExpandRatio(this.panel, 10.0F);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayout);
		this.verticalLayout.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_CENTER);
		this.horizontalLayoutShortcut.setWidth(-1, Unit.PIXELS);
		this.horizontalLayoutShortcut.setHeight(100, Unit.PERCENTAGE);
		this.verticalLayout.addComponent(this.horizontalLayoutShortcut);
		this.verticalLayout.setWidth(100, Unit.PERCENTAGE);
		this.verticalLayout.setHeight(-1, Unit.PIXELS);
		this.setContent(this.verticalLayout);
		this.setSizeFull();

		this.mnuStartStop.setCommand(selectedItem -> this.mnuStartStop_menuSelected(selectedItem));
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
		this.mnuSaveItem.setCommand(selectedItem -> this.mnuSaveItem_menuSelected(selectedItem));
		this.datePrlReportDateFrom.addValueChangeListener(event -> this.datePrlReportDateFrom_valueChange(event));
		this.btnSearch.addClickListener(event -> this.btnSearch_buttonClick(event));
		this.datePrlReportDateTo.addValueChangeListener(event -> this.datePrlReportDateTo_valueChange(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdCancel.addClickListener(event -> this.cmdCancel_buttonClick(event));
		this.cmdStartStop.addClickListener(event -> this.cmdStartStop_buttonClick(event));
		this.cmdDefault1.addClickListener(event -> this.cmdDefault1_buttonClick(event));
		this.cmd2.addClickListener(event -> this.cmd2_buttonClick(event));
		this.cmd3.addClickListener(event -> this.cmd3_buttonClick(event));
		this.cmd4.addClickListener(event -> this.cmd4_buttonClick(event));
		this.cmd5.addClickListener(event -> this.cmd5_buttonClick(event));
		this.cmd6.addClickListener(event -> this.cmd6_buttonClick(event));
		this.cmd7.addClickListener(event -> this.cmd7_buttonClick(event));
		this.cmd8.addClickListener(event -> this.cmd8_buttonClick(event));
		this.cmd9.addClickListener(event -> this.cmd9_buttonClick(event));
		this.cmd10.addClickListener(event -> this.cmd10_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel label, label3, lblPeriode, lblPrlReportDate, lblPrlFromTo, lblProject, lblPrlHours, lblPrlRate,
			lblPrlText, lblPrlWorkType, lblPrlState, label2;
	private XdevButton btnSearch, cmdSave, cmdCancel, cmdStartStop, cmdDefault1, cmd2, cmd3, cmd4, cmd5, cmd6, cmd7, cmd8,
			cmd9, cmd10;
	private XdevMenuBar menuBar;
	private XdevMenuItem menuOption, mnuStartStop, mnuSeperator2, mnuDefaults, mnuTemplate1, mnuTemplate2, mnuTemplate3,
			mnuTemplate4, mnuTemplate5, mnuTemplate6, mnuTemplate7, mnuTemplate8, mnuTemplate9, mnuTemplate10, menuText,
			mnuSeperator, mnuResetItem, mnuSaveItem;
	private XdevPanel panel;
	private XdevGridLayout form;
	private XdevComboBox<Project> cmbProject;
	private XdevComboBox<Periode> cmbPeriode;
	private XdevHorizontalLayout horizontalLayout3, horizontalLayout, horizontalLayoutShortcut;
	private XdevPopupDateField datePrlReportDate, datePrlReportDateFrom, datePrlReportDateTo;
	private XdevComboBox<?> comboBoxWorktype, comboBoxState;
	private XdevTextField txtPrlHours, txtPrlRate, txtPrlText;
	private XdevVerticalLayout verticalLayout;
	private XdevFieldGroup<ProjectLine> fieldGroup;
	// </generated-code>

}
