package ch.xwr.seicentobilling.ui.desktop;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.PersistenceException;

import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.dal.DAOs;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevCheckBox;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevMenuBar;
import com.xdev.ui.XdevMenuBar.XdevMenuItem;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevUpload;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.UploadReceiver;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.business.svc.SaveFileToDb;
import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.dal.ExpenseTemplateDAO;
import ch.xwr.seicentobilling.dal.LovAccountDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.ExpenseTemplate;
import ch.xwr.seicentobilling.entities.Expense_;
import ch.xwr.seicentobilling.entities.LovAccount;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Periode_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Project_;
import ch.xwr.seicentobilling.entities.RowImage;
import ch.xwr.seicentobilling.entities.RowObject;
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.ui.desktop.project.ProjectLookupPopup;
import ch.xwr.seicentobilling.ui.phone.AttachmentPopup;
import ch.xwr.seicentobilling.ui.phone.TextListPopup;

public class ExpensePopup extends XdevView {
	private UploadReceiver urcv = null;

	/**
	 *
	 */
	public ExpensePopup() {
		super();
		this.initUI();

		this.setHeight(Seicento.calculateThemeHeight(this.getHeight(), UI.getCurrent().getTheme()));
		this.horizontalLayoutShortcut.setWidth("2"); // active but not visible

		// State
		this.comboBoxState.addItems((Object[]) LovState.State.values());
		this.comboBoxUnit.addItems((Object[]) LovState.ExpUnit.values());
		this.comboBoxGeneric.addItems((Object[]) LovState.ExpType.values());

		// this.comboBoxAccount.addItems((Object[])LovState.Accounts.values());
		// loadDummyCb();

		// get Parameter
		final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId = (Long) UI.getCurrent().getSession().getAttribute("objId");
		Expense bean = null;
		Periode obj = null;

		if (beanId == null) {
			// new
			final PeriodeDAO objDao = new PeriodeDAO();
			obj = objDao.find(objId);

			bean = new Expense();
			bean.setExpState(LovState.State.active);
			// bean.setPrlWorkType(LovState.WorkType.project);
			bean.setExpDate(new Date());
			bean.setExpUnit(LovState.ExpUnit.stück);
			bean.setExpQuantity(new Double(1));
			bean.setExpFlagGeneric(LovState.ExpType.standard);
			bean.setExpFlagCostAccount(true);
			bean.setPeriode(obj);

		} else {
			final ExpenseDAO dao = new ExpenseDAO();
			bean = dao.find(beanId.longValue());

			prepareProjectCombo(bean.getProject());
		}

		setBeanGui(bean);
		checkTemplates();

		if (bean.getExpId() == null || bean.getExpId().floatValue() < 1) {
			this.mnuUpload.setEnabled(false);
		}

		setupUploader(new RowImage());

	}

	private void setBeanGui(final Expense bean) {
		// set Bean + Fields
		this.fieldGroup.setItemDataSource(bean);

		// set RO Fields
		setROFields();

		postLoadAccountAction(bean);
		this.txtExpText.focus();
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

	private void setROFields() {
		this.dateExpBooked.setEnabled(false);
		this.cmbPeriode.setEnabled(false);
		this.cmbProject.setEnabled(false);
	}

	private void checkTemplates() {
		final Expense line = this.fieldGroup.getItemDataSource().getBean();

		final ExpenseTemplateDAO dao = new ExpenseTemplateDAO();
		final List<ExpenseTemplate> lst = dao.findByCostAccount(line.getPeriode().getCostAccount());

		XdevMenuItem item = null;

		for (int i = 1; i < 11; i++) {
			item = getMnItem(i);
			item.setEnabled(false);
			item.setVisible(false);
		}

		if (lst == null) {
			return; // not found
		}

		for (final Iterator<ExpenseTemplate> iterator = lst.iterator(); iterator.hasNext();) {
			final ExpenseTemplate tpl = iterator.next();
			final int nbr = tpl.getExtKeyNumber();
			item = getMnItem(nbr);

			// #358
			String value = "" + nbr + ": " + tpl.getProject().getProName();
			if (tpl.getExtText() != null) {
				if (value.length() > 25) {
					value = value.substring(0, 25);
				}
				value = value + " - " + tpl.getExtText();
			}

			if (value.length() > 40) {
				value = value.substring(0, 40);
			}

			item.setEnabled(true);
			item.setVisible(true);
			item.setCaption(value);
		}

	}

	private void setupUploader(final RowImage bean) {
		this.upload.setVisible(true);
		this.upload.setEnabled(true);

		this.urcv = new UploadReceiver(bean);
		this.urcv.setResizeImage(true);
		this.upload.setReceiver(this.urcv);

		this.upload.addSucceededListener(new Upload.SucceededListener() {
			@Override
			public void uploadSucceeded(final SucceededEvent event) {
				// This method gets called when the upload finished successfully
				System.out.println("________________ UPLOAD SUCCEEDED 2");

        	    if (ExpensePopup.this.urcv.getFiup().length() >  (ExpensePopup.this.urcv.getMaxImageSize() * 2)) {
	        	    final int ikb = ExpensePopup.this.urcv.getMaxImageSize() * 4 / 1024;
	        		Notification.show("Datei ist zu gross", "Max Size: " + ikb + " KB " + ExpensePopup.this.urcv.getFiup().getName(),Type.TRAY_NOTIFICATION);
	        		ExpensePopup.this.urcv.removeUploadedFile();

        	    } else {
					ExpensePopup.this.urcv.uploadSucceeded(event);
					Notification.show("Datei hochgeladen", "Name: " + ExpensePopup.this.urcv.getFiup().getName(),
							Type.TRAY_NOTIFICATION);
					//ExpensePopup.this.upload.setButtonCaption("* Uploaded");
					ExpensePopup.this.upload.setEnabled(false);
					ExpensePopup.this.upload.setVisible(false);
        	    }
			}
		});
		// uploader
	}

	private XdevMenuItem getMnItem(final int icount) {

		switch (icount) {
		case 0:
			return this.mnuTemplate10;
		case 1:
			return this.mnuTemplate1;
		case 2:
			return this.mnuTemplate2;
		case 3:
			return this.mnuTemplate3;
		case 4:
			return this.mnuTemplate4;
		case 5:
			return this.mnuTemplate5;
		case 6:
			return this.mnuTemplate6;
		case 7:
			return this.mnuTemplate7;
		case 8:
			return this.mnuTemplate8;
		case 9:
			return this.mnuTemplate9;
		case 10:
			return this.mnuTemplate10;
		}

		return null;
	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		// win.setWidth("720");
		// win.setHeight("660");
		win.center();
		win.setModal(true);
		win.setContent(new ExpensePopup());

		return win;
	}

	private void preSaveAccountAction() {
		if (this.comboBoxAccount.getSelectedItem() != null) {
			final LovAccount lov = this.comboBoxAccount.getSelectedItem().getBean();
			if (lov != null) {
				this.fieldGroup.getItemDataSource().getBean().setExpAccount(lov.getId());
			}
		}
	}

	private void loadTemplate(final int iKey) {
		final Expense line = this.fieldGroup.getItemDataSource().getBean();

		final ExpenseTemplateDAO dao = new ExpenseTemplateDAO();
		final ExpenseTemplate tpl = dao.findByKeyNumber(line.getPeriode().getCostAccount(), iKey);

		if (tpl == null) {
			return; // not found
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

		prepareProjectCombo(tpl.getProject());
		this.fieldGroup.setItemDataSource(line);
		setROFields();

		postLoadAccountAction(line);
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
	 * {@link #mnuCancel}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuCancel_menuSelected(final MenuBar.MenuItem selectedItem) {
		this.fieldGroup.discard();
		((Window) this.getParent()).close();

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
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");

		preSaveAccountAction();
		if (SeicentoCrud.doSave(this.fieldGroup)) {
			try {
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.fieldGroup.getItemDataSource().getBean().getExpId(),
						this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());
				postSave();
				((Window) this.getParent()).close();
			} catch (final PersistenceException cx) {
				final String msg = SeicentoCrud.getPerExceptionError(cx);
				Notification.show("Fehler beim Speichern", msg, Notification.Type.ERROR_MESSAGE);
				cx.printStackTrace();
			} catch (final Exception e) {
				Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
				e.printStackTrace();
			}

		}
	}

	private void postSave() {
		if (this.urcv != null && this.urcv.getFiup() != null) {
			final RowObjectManager man = new RowObjectManager();
			final RowObject rowobj = man.getRowObject(this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName(), this.fieldGroup.getItemDataSource().getBean().getExpId());


			final File fio = this.urcv.getFiup();
			final SaveFileToDb upsave = new SaveFileToDb(fio.getAbsolutePath(), this.urcv.getMimeType(), rowobj.getObjId());
			upsave.importFile();
		}
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
					ExpensePopup.this.txtExpText.setValue(reason);
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

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuUpload}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuUpload_menuSelected(final MenuBar.MenuItem selectedItem) {
		final RowObject obj = getRowObject();
		if (obj == null) {
			return;
		}
		UI.getCurrent().getSession().setAttribute("RowObject", obj);

		popupAttachments();

	}

	private RowObject getRowObject() {
		final Expense bean = this.fieldGroup.getItemDataSource().getBean();

		if (bean.getExpId() == null) {
			return null;
		}

		final RowObjectManager man = new RowObjectManager();
		final RowObject obj = man.getRowObject(bean.getClass().getSimpleName(), bean.getExpId());
		return obj;
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
					// ExpenseView.this.txtExpText.setValue(reason);
				}

			}
		});

		this.getUI().addWindow(win);

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

	private void popupProjectLookup() {
		final Window win = ProjectLookupPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");

				if (beanId != null && beanId > 0) {
					final Project bean = new ProjectDAO().find(beanId);
					prepareProjectCombo(bean);

				}
			}
		});
		this.getUI().addWindow(win);

	}

	private void prepareProjectCombo(final Project bean) {
		ExpensePopup.this.cmbProject.addItem(bean);
		ExpensePopup.this.cmbProject.setValue(bean);
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
		this.mnuUpload = this.menuOption.addItem("Belege verwalten...", null);
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
		this.mnuCancel = this.menuOption.addItem(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this), null);
		this.mnuSaveItem = this.menuOption.addItem("Speichern", null);
		this.label = new XdevLabel();
		this.label3 = new XdevLabel();
		this.form = new XdevGridLayout();
		this.lblPeriode = new XdevLabel();
		this.cmbPeriode = new XdevComboBox<>();
		this.lblExpBooked = new XdevLabel();
		this.dateExpBooked = new XdevPopupDateField();
		this.lblExpDate = new XdevLabel();
		this.dateExpDate = new XdevPopupDateField();
		this.lblExpText = new XdevLabel();
		this.txtExpText = new XdevTextField();
		this.lblExpAmount = new XdevLabel();
		this.txtExpAmount = new XdevTextField();
		this.lblVat = new XdevLabel();
		this.cmbVat = new XdevComboBox<>();
		this.lblProject = new XdevLabel();
		this.horizontalLayoutProject = new XdevHorizontalLayout();
		this.cmbProject = new XdevComboBox<>();
		this.btnSearch = new XdevButton();
		this.lblExpAccount = new XdevLabel();
		this.comboBoxAccount = new XdevComboBox<>();
		this.lblExpFlagGeneric = new XdevLabel();
		this.comboBoxGeneric = new XdevComboBox<>();
		this.lblExpFlagCostAccount = new XdevLabel();
		this.chkExpFlagCostAccount = new XdevCheckBox();
		this.lblExpUnit = new XdevLabel();
		this.comboBoxUnit = new XdevComboBox<>();
		this.lblExpQuantity = new XdevLabel();
		this.txtExpQuantity = new XdevTextField();
		this.lblExpState = new XdevLabel();
		this.comboBoxState = new XdevComboBox<>();
		this.fieldGroup = new XdevFieldGroup<>(Expense.class);
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.upload = new XdevUpload();
		this.label2 = new XdevLabel();
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
		this.mnuUpload.setIcon(FontAwesome.UPLOAD);
		this.mnuDefaults.setIcon(FontAwesome.BOOKMARK);
		this.menuText.setIcon(FontAwesome.LIST_ALT);
		this.mnuCancel.setIcon(FontAwesome.CLOSE);
		this.mnuCancel.setCheckable(true);
		this.mnuSaveItem.setIcon(FontAwesome.SAVE);
		this.mnuSaveItem.setCheckable(true);
		this.label.setValue("Spesen erfassen");
		this.lblPeriode.setValue(StringResourceUtils.optLocalizeString("{$lblPeriode.value}", this));
		this.cmbPeriode.setContainerDataSource(Periode.class);
		this.cmbPeriode.setItemCaptionPropertyId(Periode_.perName.getName());
		this.lblExpBooked.setValue(StringResourceUtils.optLocalizeString("{$lblExpBooked.value}", this));
		this.dateExpBooked.setTabIndex(2);
		this.lblExpDate.setValue(StringResourceUtils.optLocalizeString("{$lblExpDate.value}", this));
		this.dateExpDate.setRequired(true);
		this.lblExpText.setValue(StringResourceUtils.optLocalizeString("{$lblExpText.value}", this));
		this.txtExpText.setMaxLength(128);
		this.lblExpAmount.setValue(StringResourceUtils.optLocalizeString("{$lblExpAmount.value}", this));
		this.txtExpAmount.setRequired(true);
		this.lblVat.setValue("MwSt Incl");
		this.cmbVat.setTextInputAllowed(false);
		this.cmbVat.setRequired(true);
		this.cmbVat.setItemCaptionFromAnnotation(false);
		this.cmbVat.setDescription("Mwst Sätze Inklusive");
		this.cmbVat.setContainerDataSource(Vat.class, DAOs.get(VatDAO.class).findAllInclusive());
		this.cmbVat.setItemCaptionPropertyId("fullName");
		this.lblProject.setValue(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.horizontalLayoutProject.setSpacing(false);
		this.horizontalLayoutProject.setMargin(new MarginInfo(false));
		this.cmbProject.setRequired(true);
		this.cmbProject.setEnabled(false);
		this.cmbProject.setContainerDataSource(Project.class, false);
		this.cmbProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.btnSearch.setIcon(FontAwesome.SEARCH);
		this.btnSearch.setCaption("");
		this.btnSearch.setDescription("Suchen...");
		this.lblExpAccount.setValue(StringResourceUtils.optLocalizeString("{$lblExpAccount.value}", this));
		this.comboBoxAccount.setRequired(true);
		this.comboBoxAccount.setItemCaptionFromAnnotation(false);
		this.comboBoxAccount.setContainerDataSource(LovAccount.class, DAOs.get(LovAccountDAO.class).findAllMine());
		this.comboBoxAccount.setItemCaptionPropertyId("name");
		this.lblExpFlagGeneric.setValue(StringResourceUtils.optLocalizeString("{$lblExpFlagGeneric.value}", this));
		this.lblExpFlagCostAccount.setValue(StringResourceUtils.optLocalizeString("{$lblExpFlagCostAccount.value}", this));
		this.chkExpFlagCostAccount.setCaption("");
		this.lblExpUnit.setValue(StringResourceUtils.optLocalizeString("{$lblExpUnit.value}", this));
		this.lblExpQuantity.setValue(StringResourceUtils.optLocalizeString("{$lblExpQuantity.value}", this));
		this.lblExpState.setValue(StringResourceUtils.optLocalizeString("{$lblExpState.value}", this));
		this.fieldGroup.bind(this.cmbPeriode, Expense_.periode.getName());
		this.fieldGroup.bind(this.dateExpBooked, Expense_.expBooked.getName());
		this.fieldGroup.bind(this.dateExpDate, Expense_.expDate.getName());
		this.fieldGroup.bind(this.txtExpText, Expense_.expText.getName());
		this.fieldGroup.bind(this.cmbProject, Expense_.project.getName());
		this.fieldGroup.bind(this.txtExpAmount, Expense_.expAmount.getName());
		this.fieldGroup.bind(this.cmbVat, Expense_.vat.getName());
		this.fieldGroup.bind(this.chkExpFlagCostAccount, Expense_.expFlagCostAccount.getName());
		this.fieldGroup.bind(this.comboBoxGeneric, Expense_.expFlagGeneric.getName());
		this.fieldGroup.bind(this.comboBoxUnit, Expense_.expUnit.getName());
		this.fieldGroup.bind(this.txtExpQuantity, Expense_.expQuantity.getName());
		this.fieldGroup.bind(this.comboBoxState, Expense_.expState.getName());
		this.horizontalLayout.setMargin(new MarginInfo(false, true, true, true));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdReset.setIcon(FontAwesome.CLOSE);
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.upload.setButtonCaption("Beleg...");
		this.upload.setImmediate(true);
		this.label2.setValue("            ");
		this.cmdDefault1.setIcon(FontAwesome.BOOKMARK);
		this.cmdDefault1.setCaption("Def 1");
		this.cmdDefault1.setClickShortcut(ShortcutAction.KeyCode.NUM1, ShortcutAction.ModifierKey.CTRL);
		this.horizontalLayoutShortcut.setSpacing(false);
		this.horizontalLayoutShortcut.setMargin(new MarginInfo(true, false, false, true));
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
		this.cmbProject.setWidth(100, Unit.PERCENTAGE);
		this.cmbProject.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutProject.addComponent(this.cmbProject);
		this.horizontalLayoutProject.setExpandRatio(this.cmbProject, 55.0F);
		this.btnSearch.setSizeUndefined();
		this.horizontalLayoutProject.addComponent(this.btnSearch);
		this.horizontalLayoutProject.setExpandRatio(this.btnSearch, 20.0F);
		this.form.setColumns(4);
		this.form.setRows(10);
		this.lblPeriode.setSizeUndefined();
		this.form.addComponent(this.lblPeriode, 0, 0);
		this.cmbPeriode.setWidth(100, Unit.PERCENTAGE);
		this.cmbPeriode.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbPeriode, 1, 0);
		this.lblExpBooked.setSizeUndefined();
		this.form.addComponent(this.lblExpBooked, 2, 0);
		this.dateExpBooked.setSizeUndefined();
		this.form.addComponent(this.dateExpBooked, 3, 0);
		this.lblExpDate.setSizeUndefined();
		this.form.addComponent(this.lblExpDate, 0, 1);
		this.dateExpDate.setSizeUndefined();
		this.form.addComponent(this.dateExpDate, 1, 1);
		this.lblExpText.setSizeUndefined();
		this.form.addComponent(this.lblExpText, 0, 2);
		this.txtExpText.setWidth(100, Unit.PERCENTAGE);
		this.txtExpText.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtExpText, 1, 2, 3, 2);
		this.lblExpAmount.setSizeUndefined();
		this.form.addComponent(this.lblExpAmount, 0, 3);
		this.txtExpAmount.setSizeUndefined();
		this.form.addComponent(this.txtExpAmount, 1, 3);
		this.lblVat.setSizeUndefined();
		this.form.addComponent(this.lblVat, 2, 3);
		this.cmbVat.setWidth(100, Unit.PERCENTAGE);
		this.cmbVat.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbVat, 3, 3);
		this.lblProject.setSizeUndefined();
		this.form.addComponent(this.lblProject, 0, 4);
		this.horizontalLayoutProject.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutProject.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.horizontalLayoutProject, 1, 4, 3, 4);
		this.lblExpAccount.setSizeUndefined();
		this.form.addComponent(this.lblExpAccount, 0, 5);
		this.comboBoxAccount.setSizeUndefined();
		this.form.addComponent(this.comboBoxAccount, 1, 5);
		this.lblExpFlagGeneric.setSizeUndefined();
		this.form.addComponent(this.lblExpFlagGeneric, 0, 6);
		this.comboBoxGeneric.setSizeUndefined();
		this.form.addComponent(this.comboBoxGeneric, 1, 6);
		this.lblExpFlagCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblExpFlagCostAccount, 2, 6);
		this.chkExpFlagCostAccount.setWidth(100, Unit.PERCENTAGE);
		this.chkExpFlagCostAccount.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.chkExpFlagCostAccount, 3, 6);
		this.lblExpUnit.setSizeUndefined();
		this.form.addComponent(this.lblExpUnit, 0, 7);
		this.comboBoxUnit.setSizeUndefined();
		this.form.addComponent(this.comboBoxUnit, 1, 7);
		this.lblExpQuantity.setSizeUndefined();
		this.form.addComponent(this.lblExpQuantity, 2, 7);
		this.txtExpQuantity.setWidth(100, Unit.PERCENTAGE);
		this.txtExpQuantity.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtExpQuantity, 3, 7);
		this.lblExpState.setSizeUndefined();
		this.form.addComponent(this.lblExpState, 0, 8);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 8);
		this.form.setColumnExpandRatio(1, 70.0F);
		this.form.setColumnExpandRatio(3, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 9, 3, 9);
		this.form.setRowExpandRatio(9, 1.0F);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_RIGHT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_RIGHT);
		this.upload.setSizeUndefined();
		this.horizontalLayout.addComponent(this.upload);
		this.horizontalLayout.setComponentAlignment(this.upload, Alignment.MIDDLE_CENTER);
		this.label2.setWidth(100, Unit.PIXELS);
		this.label2.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout.addComponent(this.label2);
		this.horizontalLayout.setComponentAlignment(this.label2, Alignment.MIDDLE_CENTER);
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
		this.form.setWidth(100, Unit.PERCENTAGE);
		this.form.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.form);
		this.verticalLayout.setComponentAlignment(this.form, Alignment.MIDDLE_CENTER);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayout);
		this.verticalLayout.setComponentAlignment(this.horizontalLayout, Alignment.TOP_RIGHT);
		this.horizontalLayoutShortcut.setWidth(-1, Unit.PIXELS);
		this.horizontalLayoutShortcut.setHeight(100, Unit.PERCENTAGE);
		this.verticalLayout.addComponent(this.horizontalLayoutShortcut);
		this.verticalLayout.setWidth(100, Unit.PERCENTAGE);
		this.verticalLayout.setHeight(-1, Unit.PIXELS);
		this.setContent(this.verticalLayout);
		this.setWidth(740, Unit.PIXELS);
		this.setHeight(660, Unit.PIXELS);

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
		this.mnuCancel.setCommand(selectedItem -> this.mnuCancel_menuSelected(selectedItem));
		this.mnuSaveItem.setCommand(selectedItem -> this.mnuSaveItem_menuSelected(selectedItem));
		this.btnSearch.addClickListener(event -> this.btnSearch_buttonClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
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
	private XdevLabel label, label3, lblPeriode, lblExpBooked, lblExpDate, lblExpText, lblExpAmount, lblVat, lblProject,
			lblExpAccount, lblExpFlagGeneric, lblExpFlagCostAccount, lblExpUnit, lblExpQuantity, lblExpState, label2;
	private XdevButton btnSearch, cmdSave, cmdReset, cmdDefault1, cmd2, cmd3, cmd4, cmd5, cmd6, cmd7, cmd8, cmd9, cmd10;
	private XdevMenuBar menuBar;
	private XdevMenuItem menuOption, mnuUpload, mnuDefaults, mnuTemplate1, mnuTemplate2, mnuTemplate3, mnuTemplate4,
			mnuTemplate5, mnuTemplate6, mnuTemplate7, mnuTemplate8, mnuTemplate9, mnuTemplate10, menuText, mnuSeperator,
			mnuCancel, mnuSaveItem;
	private XdevFieldGroup<Expense> fieldGroup;
	private XdevGridLayout form;
	private XdevComboBox<Project> cmbProject;
	private XdevComboBox<Periode> cmbPeriode;
	private XdevUpload upload;
	private XdevHorizontalLayout horizontalLayout3, horizontalLayoutProject, horizontalLayout, horizontalLayoutShortcut;
	private XdevComboBox<Vat> cmbVat;
	private XdevPopupDateField dateExpBooked, dateExpDate;
	private XdevComboBox<?> comboBoxGeneric, comboBoxUnit, comboBoxState;
	private XdevCheckBox chkExpFlagCostAccount;
	private XdevTextField txtExpText, txtExpAmount, txtExpQuantity;
	private XdevVerticalLayout verticalLayout;
	private XdevComboBox<LovAccount> comboBoxAccount;
	// </generated-code>

}
