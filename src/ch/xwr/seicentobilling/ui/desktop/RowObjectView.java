package ch.xwr.seicentobilling.ui.desktop;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.dal.DAOs;
import com.xdev.res.ApplicationResource;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevVerticalSplitPanel;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.util.NestedProperty;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.dal.DatabaseVersionDAO;
import ch.xwr.seicentobilling.dal.EntityDAO;
import ch.xwr.seicentobilling.dal.RowImageDAO;
import ch.xwr.seicentobilling.dal.RowObjectDAO;
import ch.xwr.seicentobilling.dal.RowParameterDAO;
import ch.xwr.seicentobilling.dal.RowTextDAO;
import ch.xwr.seicentobilling.entities.DatabaseVersion;
import ch.xwr.seicentobilling.entities.DatabaseVersion_;
import ch.xwr.seicentobilling.entities.Entity;
import ch.xwr.seicentobilling.entities.Entity_;
import ch.xwr.seicentobilling.entities.RowImage;
import ch.xwr.seicentobilling.entities.RowImage_;
import ch.xwr.seicentobilling.entities.RowObject;
import ch.xwr.seicentobilling.entities.RowObject_;
import ch.xwr.seicentobilling.entities.RowParameter;
import ch.xwr.seicentobilling.entities.RowParameter_;
import ch.xwr.seicentobilling.entities.RowText;
//import ch.xwr.seicentobookit.ui.FunctionUpDownloadRowImage.Generator;
import ch.xwr.seicentobilling.entities.RowText_;

public class RowObjectView extends XdevView {

	/**
	 *
	 */
	public RowObjectView() {
		super();
		this.initUI();
	}

	/**
	 *
	 */
	public RowObjectView(final long rowid, final String entName) {
		super();
		this.initUI();

		loadUI(rowid, entName);
	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		win.setWidth("1140");
		win.setHeight("760");
		win.center();
		win.setModal(true);
		return win;
	}


	private void loadUI(final long rowid, final String entName) {
		final RowObjectDAO objDao = new RowObjectDAO();
		final RowObject obj = objDao.getObjectBase(entName, rowid);

		if (obj == null) {
			Notification.show("Objektstamm", "Kein Record gefunden!", Notification.Type.WARNING_MESSAGE);
		}

		this.fieldGroup.setItemDataSource(obj);

		//2nd ObjText
		this.tableText.getBeanContainerDataSource().addContainerFilter(new Compare.Equal("rowObject", obj));
		this.tableText.getBeanContainerDataSource().refresh();

		this.tableRowImage.getBeanContainerDataSource().addContainerFilter(new Compare.Equal("rowObject", obj));
		this.tableRowImage.getBeanContainerDataSource().refresh();

		this.tableRowParam.getBeanContainerDataSource().addContainerFilter(new Compare.Equal("rowObject", obj));
		this.tableRowParam.getBeanContainerDataSource().refresh();
	}

	private void popupRowText() {
		final Window win = RowTextPopup.getPopupWindow();
		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				//Dummy for reloading
				RowObjectView.this.tableText.refreshRowCache();
				RowObjectView.this.tableText.sort();
				RowObjectView.this.tableText.getBeanContainerDataSource().refresh();
			}
		});
		this.getUI().addWindow(win);
	}

	private void popupRowImage() {
		final Window win = RowFilePopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				//Dummy for reloading
				RowObjectView.this.tableRowImage.refreshRowCache();
				RowObjectView.this.tableRowImage.sort();
				RowObjectView.this.tableRowImage.getBeanContainerDataSource().refresh();
			}
		});
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdNewText}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewText_buttonClick(final Button.ClickEvent event) {
		final Long beanId = null;
		final Long objId = this.fieldGroup.getItemDataSource().getBean().getObjId();

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupRowText();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdUpdateText}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdateText_buttonClick(final Button.ClickEvent event) {
		if (this.tableText.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.tableText.getSelectedItem().getBean().getTxtId();
		final Long objId = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupRowText();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDeleteText}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteText_buttonClick(final Button.ClickEvent event) {
		if (this.tableText.getSelectedItem() == null) {
			Notification.show("Datensatz löschen", "Es wurde keine Zeile selektiert in der Tabelle", Notification.Type.WARNING_MESSAGE);
			return;
		}

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
				final RowText bean = RowObjectView.this.tableText.getSelectedItem().getBean();
				//Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getTxtId(), bean.getClass().getSimpleName());

				final RowTextDAO dao = new RowTextDAO();
				dao.remove(bean);
				RowObjectView.this.tableText.getBeanContainerDataSource().refresh();

				if (!RowObjectView.this.tableText.isEmpty() ) {
					//tableText.select(tableText.getCurrentPageFirstItemId());
				}
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!", Notification.Type.TRAY_NOTIFICATION);
			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdNewFile}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewFile_buttonClick(final Button.ClickEvent event) {
		final Long beanId = null;
		final Long objId = this.fieldGroup.getItemDataSource().getBean().getObjId();

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupRowImage();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdUpdateFile}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdateFile_buttonClick(final Button.ClickEvent event) {
		if (this.tableRowImage.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.tableRowImage.getSelectedItem().getBean().getRimId();
		final Long objId = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupRowImage();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDeleteFile}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteFile_buttonClick(final Button.ClickEvent event) {
		final XdevTable<RowImage> tab = this.tableRowImage;
		if (tab.getSelectedItem() == null) {
			Notification.show("Datensatz löschen", "Es wurde keine Zeile selektiert in der Tabelle", Notification.Type.WARNING_MESSAGE);
			return;
		}
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
				final RowImage bean = tab.getSelectedItem().getBean();
				//Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getRimId(), bean.getClass().getSimpleName());

				final RowImageDAO dao = new RowImageDAO();
				dao.remove(bean);
				tab.getBeanContainerDataSource().refresh();

				//if (!tab.isEmpty()) tab.select(tab.getCurrentPageFirstItemId());
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!", Notification.Type.TRAY_NOTIFICATION);
			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdNewParam}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewParam_buttonClick(final Button.ClickEvent event) {
		final Long beanId = null;
		final Long objId = this.fieldGroup.getItemDataSource().getBean().getObjId();

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupRowParam();

	}

	private void popupRowParam() {
		final Window win = RowParamPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				//Dummy for reloading
				RowObjectView.this.tableRowParam.refreshRowCache();
				RowObjectView.this.tableRowParam.sort();
				RowObjectView.this.tableRowParam.getBeanContainerDataSource().refresh();
			}
		});
		this.getUI().addWindow(win);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDeleteParam}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteParam_buttonClick(final Button.ClickEvent event) {
		final XdevTable<RowParameter> tab = this.tableRowParam;
		if (tab.getSelectedItem() == null) {
			Notification.show("Datensatz löschen", "Es wurde keine Zeile selektiert in der Tabelle", Notification.Type.WARNING_MESSAGE);
			return;
		}
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
				final RowParameter bean = tab.getSelectedItem().getBean();
				//Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getPrmId(), bean.getClass().getSimpleName());

				final RowParameterDAO dao = new RowParameterDAO();
				dao.remove(bean);
				tab.getBeanContainerDataSource().refresh();

				//if (!tab.isEmpty()) tab.select(tab.getCurrentPageFirstItemId());
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!", Notification.Type.TRAY_NOTIFICATION);
			}

		});


	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdUpdateParam}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdateParam_buttonClick(final Button.ClickEvent event) {
		if (this.tableRowParam.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.tableRowParam.getSelectedItem().getBean().getPrmId();
		final Long objId = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupRowParam();
	}

	/**
	 * Event handler delegate method for the {@link XdevTable}
	 * {@link #tableRowImage}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableRowImage_valueChange(final Property.ValueChangeEvent event) {
		if (this.tableRowImage.getSelectedItem() == null) {
			return;
		}

		final RowImage rof = this.tableRowImage.getSelectedItem().getBean();
		boolean editable = true;
		if (rof.getRimNumber() == 100) {
			//Archive
			editable = false;
		}

		this.cmdDeleteFile.setEnabled(editable);
		this.cmdUpdateFile.setEnabled(editable);
	}

	/**
	 * Event handler delegate method for the {@link XdevTable}
	 * {@link #tableRowParam}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableRowParam_itemClick(final ItemClickEvent event) {
		if (event.isDoubleClick()) {
			@SuppressWarnings("unchecked")
			final BeanItem<RowParameter> x = (BeanItem<RowParameter>) event.getItem();
			final Long beanId = x.getBean().getPrmId(); //this.tableRowParam.getSelectedItem().getBean().getPrmId();
			final Long objId = null;

			UI.getCurrent().getSession().setAttribute("beanId", beanId);
			UI.getCurrent().getSession().setAttribute("objId", objId);

			popupRowParam();
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #tableText}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableText_itemClick(final ItemClickEvent event) {
		if (event.isDoubleClick()) {
			@SuppressWarnings("unchecked")
			final BeanItem<RowText> x = (BeanItem<RowText>) event.getItem();
			final Long beanId = x.getBean().getTxtId(); //this.tableRowParam.getSelectedItem().getBean().getPrmId();
			final Long objId = null;

			UI.getCurrent().getSession().setAttribute("beanId", beanId);
			UI.getCurrent().getSession().setAttribute("objId", objId);

			popupRowText();
		}

	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalSplitPanel = new XdevVerticalSplitPanel();
		this.panel = new XdevPanel();
		this.form = new XdevGridLayout();
		this.lblObjId = new XdevLabel();
		this.txtObjId = new XdevTextField();
		this.lblObjAdded = new XdevLabel();
		this.dateObjAdded = new XdevPopupDateField();
		this.lblObjDeleted = new XdevLabel();
		this.dateObjDeleted = new XdevPopupDateField();
		this.lblEntity = new XdevLabel();
		this.cmbEntity = new XdevComboBox<>();
		this.lblObjAddedBy = new XdevLabel();
		this.txtObjAddedBy = new XdevTextField();
		this.lblObjDeletedBy = new XdevLabel();
		this.txtObjDeletedBy = new XdevTextField();
		this.lblObjRowId = new XdevLabel();
		this.txtObjRowId = new XdevTextField();
		this.lblObjChanged = new XdevLabel();
		this.dateObjChanged = new XdevPopupDateField();
		this.lblDatabaseVersion = new XdevLabel();
		this.cmbDatabaseVersion = new XdevComboBox<>();
		this.lblObjChngcnt = new XdevLabel();
		this.txtObjChngcnt = new XdevTextField();
		this.lblObjChangedBy = new XdevLabel();
		this.txtObjChangedBy = new XdevTextField();
		this.lblObjState = new XdevLabel();
		this.txtObjState = new XdevTextField();
		this.fieldGroup = new XdevFieldGroup<>(RowObject.class);
		this.tabSheet = new XdevTabSheet();
		this.verticalLayoutText = new XdevVerticalLayout();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.cmdNewText = new XdevButton();
		this.cmdDeleteText = new XdevButton();
		this.cmdUpdateText = new XdevButton();
		this.tableText = new XdevTable<>();
		this.verticalLayoutFile = new XdevVerticalLayout();
		this.actionLayout = new XdevHorizontalLayout();
		this.cmdNewFile = new XdevButton();
		this.cmdDeleteFile = new XdevButton();
		this.cmdUpdateFile = new XdevButton();
		this.tableRowImage = new XdevTable<>();
		this.verticalLayoutParam = new XdevVerticalLayout();
		this.actionLayoutParam = new XdevHorizontalLayout();
		this.cmdNewParam = new XdevButton();
		this.cmdDeleteParam = new XdevButton();
		this.cmdUpdateParam = new XdevButton();
		this.tableRowParam = new XdevTable<>();

		this.setCaption(StringResourceUtils.optLocalizeString("{$RowObjectView.caption}", this));
		this.verticalSplitPanel.setSplitPosition(33.0F, Unit.PERCENTAGE);
		this.lblObjId.setValue(StringResourceUtils.optLocalizeString("{$lblObjId.value}", this));
		this.txtObjId.setTabIndex(1);
		this.lblObjAdded.setValue(StringResourceUtils.optLocalizeString("{$lblObjAdded.value}", this));
		this.dateObjAdded.setTabIndex(5);
		this.dateObjAdded.setResolution(Resolution.SECOND);
		this.lblObjDeleted.setValue(StringResourceUtils.optLocalizeString("{$lblObjDeleted.value}", this));
		this.dateObjDeleted.setTabIndex(9);
		this.dateObjDeleted.setResolution(Resolution.SECOND);
		this.lblEntity.setValue(StringResourceUtils.optLocalizeString("{$lblEntity.value}", this));
		this.cmbEntity.setTabIndex(2);
		this.cmbEntity.setContainerDataSource(Entity.class, DAOs.get(EntityDAO.class).findAll());
		this.cmbEntity.setItemCaptionPropertyId(Entity_.entName.getName());
		this.lblObjAddedBy.setValue(StringResourceUtils.optLocalizeString("{$lblObjAddedBy.value}", this));
		this.txtObjAddedBy.setTabIndex(6);
		this.lblObjDeletedBy.setValue(StringResourceUtils.optLocalizeString("{$lblObjDeletedBy.value}", this));
		this.txtObjDeletedBy.setTabIndex(10);
		this.lblObjRowId.setValue(StringResourceUtils.optLocalizeString("{$lblObjRowId.value}", this));
		this.txtObjRowId.setTabIndex(3);
		this.lblObjChanged.setValue(StringResourceUtils.optLocalizeString("{$lblObjChanged.value}", this));
		this.dateObjChanged.setTabIndex(7);
		this.dateObjChanged.setResolution(Resolution.SECOND);
		this.lblDatabaseVersion.setValue(StringResourceUtils.optLocalizeString("{$lblDatabaseVersion.value}", this));
		this.cmbDatabaseVersion.setTabIndex(11);
		this.cmbDatabaseVersion.setContainerDataSource(DatabaseVersion.class, DAOs.get(DatabaseVersionDAO.class).findAll());
		this.cmbDatabaseVersion.setItemCaptionPropertyId(DatabaseVersion_.dbvMicro.getName());
		this.lblObjChngcnt.setValue(StringResourceUtils.optLocalizeString("{$lblObjChngcnt.value}", this));
		this.txtObjChngcnt.setTabIndex(4);
		this.lblObjChangedBy.setValue(StringResourceUtils.optLocalizeString("{$lblObjChangedBy.value}", this));
		this.txtObjChangedBy.setTabIndex(8);
		this.lblObjState.setValue(StringResourceUtils.optLocalizeString("{$lblObjState.value}", this));
		this.txtObjState.setTabIndex(12);
		this.fieldGroup.setReadOnly(true);
		this.fieldGroup.bind(this.txtObjId, RowObject_.objId.getName());
		this.fieldGroup.bind(this.cmbEntity, RowObject_.entity.getName());
		this.fieldGroup.bind(this.txtObjRowId, RowObject_.objRowId.getName());
		this.fieldGroup.bind(this.txtObjChngcnt, RowObject_.objChngcnt.getName());
		this.fieldGroup.bind(this.dateObjAdded, RowObject_.objAdded.getName());
		this.fieldGroup.bind(this.txtObjAddedBy, RowObject_.objAddedBy.getName());
		this.fieldGroup.bind(this.dateObjChanged, RowObject_.objChanged.getName());
		this.fieldGroup.bind(this.txtObjChangedBy, RowObject_.objChangedBy.getName());
		this.fieldGroup.bind(this.dateObjDeleted, RowObject_.objDeleted.getName());
		this.fieldGroup.bind(this.txtObjDeletedBy, RowObject_.objDeletedBy.getName());
		this.fieldGroup.bind(this.cmbDatabaseVersion, RowObject_.databaseVersion.getName());
		this.fieldGroup.bind(this.txtObjState, RowObject_.objState.getName());
		this.tabSheet.setStyleName("framed");
		this.horizontalLayout2.setSpacing(false);
		this.horizontalLayout2.setMargin(new MarginInfo(false, true, false, false));
		this.cmdNewText
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/new1_16.png"));
		this.cmdNewText.setCaption("New");
		this.cmdDeleteText
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.cmdDeleteText.setCaption(StringResourceUtils.optLocalizeString("{$cmdDeleteText.caption}", this));
		this.cmdUpdateText
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/edit1.png"));
		this.cmdUpdateText.setCaption(StringResourceUtils.optLocalizeString("{$cmdUpdateText.caption}", this));
		this.tableText.setContainerDataSource(RowText.class);
		this.tableText.setVisibleColumns(RowText_.txtNumber.getName(), RowText_.txtFreetext.getName(),
				RowText_.language.getName(), RowText_.txtState.getName());
		this.tableText.setColumnHeader("txtNumber", "Nbr");
		this.tableText.setColumnWidth("txtNumber", 60);
		this.tableText.setColumnHeader("txtFreetext", "Text");
		this.tableText.setColumnHeader("language", "Sprache");
		this.tableText.setColumnHeader("txtState", "Status");
		this.actionLayout.setSpacing(false);
		this.actionLayout.setMargin(new MarginInfo(false, true, false, false));
		this.cmdNewFile
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/new1_16.png"));
		this.cmdNewFile.setCaption(StringResourceUtils.optLocalizeString("{$cmdNewFile.caption}", this));
		this.cmdDeleteFile
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.cmdDeleteFile.setCaption(StringResourceUtils.optLocalizeString("{$cmdDeleteFile.caption}", this));
		this.cmdUpdateFile
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/edit1.png"));
		this.cmdUpdateFile.setCaption(StringResourceUtils.optLocalizeString("{$cmdUpdateFile.caption}", this));
		this.tableRowImage.setContainerDataSource(RowImage.class);
		this.tableRowImage.addGeneratedColumn("generated", new FunctionUpDownloadRowFile.Generator());
		this.tableRowImage.setVisibleColumns(RowImage_.rimNumber.getName(), RowImage_.rimName.getName(),
				RowImage_.rimSize.getName(), RowImage_.rimType.getName(), RowImage_.rimMimetype.getName(),
				RowImage_.rimState.getName(), "generated");
		this.tableRowImage.setColumnHeader("rimNumber", "Nbr");
		this.tableRowImage.setColumnWidth("rimNumber", 60);
		this.tableRowImage.setColumnHeader("rimName", "Name");
		this.tableRowImage.setColumnHeader("rimSize", "Grösse");
		this.tableRowImage.setColumnHeader("rimType", "Typ");
		this.tableRowImage.setColumnHeader("rimMimetype", "Mime Typ");
		this.tableRowImage.setColumnHeader("rimState", "Status");
		this.tableRowImage.setColumnHeader("generated", "...");
		this.actionLayoutParam.setSpacing(false);
		this.actionLayoutParam.setMargin(new MarginInfo(false, true, false, false));
		this.cmdNewParam
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/new1_16.png"));
		this.cmdNewParam.setCaption(StringResourceUtils.optLocalizeString("{$cmdNewFile.caption}", this));
		this.cmdDeleteParam
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.cmdDeleteParam.setCaption(StringResourceUtils.optLocalizeString("{$cmdDeleteFile.caption}", this));
		this.cmdUpdateParam
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/edit1.png"));
		this.cmdUpdateParam.setCaption(StringResourceUtils.optLocalizeString("{$cmdUpdateFile.caption}", this));
		this.tableRowParam.setColumnReorderingAllowed(true);
		this.tableRowParam.setColumnCollapsingAllowed(true);
		this.tableRowParam.setContainerDataSource(RowParameter.class,
				NestedProperty.of(RowParameter_.rowObject, RowObject_.entity, Entity_.entName));
		this.tableRowParam.setVisibleColumns(RowParameter_.prmValue.getName(), RowParameter_.prmGroup.getName(),
				RowParameter_.prmSubGroup.getName(), RowParameter_.prmKey.getName(), RowParameter_.prmValueType.getName(),
				RowParameter_.prmState.getName(),
				NestedProperty.path(RowParameter_.rowObject, RowObject_.entity, Entity_.entName));
		this.tableRowParam.setColumnHeader("prmValue", "Wert");
		this.tableRowParam.setColumnHeader("prmGroup", "Gruppe");
		this.tableRowParam.setColumnHeader("prmSubGroup", "Untergruppe");
		this.tableRowParam.setColumnHeader("prmKey", "Schlüssel");
		this.tableRowParam.setColumnHeader("prmValueType", "Type");
		this.tableRowParam.setColumnCollapsed("prmValueType", true);
		this.tableRowParam.setColumnHeader("prmState", "Status");
		this.tableRowParam.setColumnCollapsed("prmState", true);
		this.tableRowParam.setColumnHeader("rowObject.entity.entName", "Tabelle");
		this.tableRowParam.setColumnCollapsed("rowObject.entity.entName", true);

		this.form.setColumns(6);
		this.form.setRows(5);
		this.lblObjId.setSizeUndefined();
		this.form.addComponent(this.lblObjId, 0, 0);
		this.txtObjId.setSizeUndefined();
		this.form.addComponent(this.txtObjId, 1, 0);
		this.lblObjAdded.setSizeUndefined();
		this.form.addComponent(this.lblObjAdded, 2, 0);
		this.dateObjAdded.setWidth(100, Unit.PERCENTAGE);
		this.dateObjAdded.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.dateObjAdded, 3, 0);
		this.lblObjDeleted.setSizeUndefined();
		this.form.addComponent(this.lblObjDeleted, 4, 0);
		this.dateObjDeleted.setWidth(100, Unit.PERCENTAGE);
		this.dateObjDeleted.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.dateObjDeleted, 5, 0);
		this.lblEntity.setSizeUndefined();
		this.form.addComponent(this.lblEntity, 0, 1);
		this.cmbEntity.setSizeUndefined();
		this.form.addComponent(this.cmbEntity, 1, 1);
		this.lblObjAddedBy.setSizeUndefined();
		this.form.addComponent(this.lblObjAddedBy, 2, 1);
		this.txtObjAddedBy.setWidth(100, Unit.PERCENTAGE);
		this.txtObjAddedBy.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtObjAddedBy, 3, 1);
		this.lblObjDeletedBy.setSizeUndefined();
		this.form.addComponent(this.lblObjDeletedBy, 4, 1);
		this.txtObjDeletedBy.setWidth(100, Unit.PERCENTAGE);
		this.txtObjDeletedBy.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtObjDeletedBy, 5, 1);
		this.lblObjRowId.setSizeUndefined();
		this.form.addComponent(this.lblObjRowId, 0, 2);
		this.txtObjRowId.setSizeUndefined();
		this.form.addComponent(this.txtObjRowId, 1, 2);
		this.lblObjChanged.setSizeUndefined();
		this.form.addComponent(this.lblObjChanged, 2, 2);
		this.dateObjChanged.setWidth(100, Unit.PERCENTAGE);
		this.dateObjChanged.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.dateObjChanged, 3, 2);
		this.lblDatabaseVersion.setSizeUndefined();
		this.form.addComponent(this.lblDatabaseVersion, 4, 2);
		this.cmbDatabaseVersion.setWidth(100, Unit.PERCENTAGE);
		this.cmbDatabaseVersion.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbDatabaseVersion, 5, 2);
		this.lblObjChngcnt.setSizeUndefined();
		this.form.addComponent(this.lblObjChngcnt, 0, 3);
		this.txtObjChngcnt.setSizeUndefined();
		this.form.addComponent(this.txtObjChngcnt, 1, 3);
		this.lblObjChangedBy.setSizeUndefined();
		this.form.addComponent(this.lblObjChangedBy, 2, 3);
		this.txtObjChangedBy.setWidth(100, Unit.PERCENTAGE);
		this.txtObjChangedBy.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtObjChangedBy, 3, 3);
		this.lblObjState.setSizeUndefined();
		this.form.addComponent(this.lblObjState, 4, 3);
		this.txtObjState.setWidth(100, Unit.PERCENTAGE);
		this.txtObjState.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtObjState, 5, 3);
		this.form.setColumnExpandRatio(1, 60.0F);
		this.form.setColumnExpandRatio(3, 90.0F);
		this.form.setColumnExpandRatio(5, 80.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 4, 5, 4);
		this.form.setRowExpandRatio(4, 1.0F);
		this.form.setWidth(-1, Unit.PIXELS);
		this.form.setHeight(100, Unit.PERCENTAGE);
		this.panel.setContent(this.form);
		this.cmdNewText.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdNewText);
		this.horizontalLayout2.setComponentAlignment(this.cmdNewText, Alignment.MIDDLE_CENTER);
		this.cmdDeleteText.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdDeleteText);
		this.horizontalLayout2.setComponentAlignment(this.cmdDeleteText, Alignment.MIDDLE_CENTER);
		this.cmdUpdateText.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdUpdateText);
		this.horizontalLayout2.setComponentAlignment(this.cmdUpdateText, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayout2_spacer = new CustomComponent();
		horizontalLayout2_spacer.setSizeFull();
		this.horizontalLayout2.addComponent(horizontalLayout2_spacer);
		this.horizontalLayout2.setExpandRatio(horizontalLayout2_spacer, 1.0F);
		this.horizontalLayout2.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout2.setHeight(-1, Unit.PIXELS);
		this.verticalLayoutText.addComponent(this.horizontalLayout2);
		this.verticalLayoutText.setComponentAlignment(this.horizontalLayout2, Alignment.MIDDLE_CENTER);
		this.tableText.setSizeFull();
		this.verticalLayoutText.addComponent(this.tableText);
		this.verticalLayoutText.setComponentAlignment(this.tableText, Alignment.MIDDLE_CENTER);
		this.verticalLayoutText.setExpandRatio(this.tableText, 100.0F);
		this.cmdNewFile.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdNewFile);
		this.actionLayout.setComponentAlignment(this.cmdNewFile, Alignment.MIDDLE_LEFT);
		this.cmdDeleteFile.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdDeleteFile);
		this.actionLayout.setComponentAlignment(this.cmdDeleteFile, Alignment.MIDDLE_CENTER);
		this.cmdUpdateFile.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdUpdateFile);
		this.actionLayout.setComponentAlignment(this.cmdUpdateFile, Alignment.MIDDLE_CENTER);
		this.actionLayout.setSizeUndefined();
		this.verticalLayoutFile.addComponent(this.actionLayout);
		this.verticalLayoutFile.setComponentAlignment(this.actionLayout, Alignment.MIDDLE_LEFT);
		this.tableRowImage.setSizeFull();
		this.verticalLayoutFile.addComponent(this.tableRowImage);
		this.verticalLayoutFile.setComponentAlignment(this.tableRowImage, Alignment.MIDDLE_CENTER);
		this.verticalLayoutFile.setExpandRatio(this.tableRowImage, 100.0F);
		this.cmdNewParam.setSizeUndefined();
		this.actionLayoutParam.addComponent(this.cmdNewParam);
		this.actionLayoutParam.setComponentAlignment(this.cmdNewParam, Alignment.MIDDLE_LEFT);
		this.cmdDeleteParam.setSizeUndefined();
		this.actionLayoutParam.addComponent(this.cmdDeleteParam);
		this.actionLayoutParam.setComponentAlignment(this.cmdDeleteParam, Alignment.MIDDLE_CENTER);
		this.cmdUpdateParam.setSizeUndefined();
		this.actionLayoutParam.addComponent(this.cmdUpdateParam);
		this.actionLayoutParam.setComponentAlignment(this.cmdUpdateParam, Alignment.MIDDLE_CENTER);
		this.actionLayoutParam.setSizeUndefined();
		this.verticalLayoutParam.addComponent(this.actionLayoutParam);
		this.verticalLayoutParam.setComponentAlignment(this.actionLayoutParam, Alignment.MIDDLE_LEFT);
		this.tableRowParam.setSizeFull();
		this.verticalLayoutParam.addComponent(this.tableRowParam);
		this.verticalLayoutParam.setComponentAlignment(this.tableRowParam, Alignment.MIDDLE_CENTER);
		this.verticalLayoutParam.setExpandRatio(this.tableRowParam, 100.0F);
		this.verticalLayoutText.setSizeFull();
		this.tabSheet.addTab(this.verticalLayoutText, "Text", null);
		this.verticalLayoutFile.setSizeFull();
		this.tabSheet.addTab(this.verticalLayoutFile, "Dateien", null);
		this.verticalLayoutParam.setSizeFull();
		this.tabSheet.addTab(this.verticalLayoutParam, "Parameter", null);
		this.tabSheet.setSelectedTab(this.verticalLayoutText);
		this.panel.setSizeFull();
		this.verticalSplitPanel.setFirstComponent(this.panel);
		this.tabSheet.setSizeFull();
		this.verticalSplitPanel.setSecondComponent(this.tabSheet);
		this.verticalSplitPanel.setSizeFull();
		this.setContent(this.verticalSplitPanel);
		this.setSizeFull();

		this.cmdNewText.addClickListener(event -> this.cmdNewText_buttonClick(event));
		this.cmdDeleteText.addClickListener(event -> this.cmdDeleteText_buttonClick(event));
		this.cmdUpdateText.addClickListener(event -> this.cmdUpdateText_buttonClick(event));
		this.tableText.addItemClickListener(event -> this.tableText_itemClick(event));
		this.cmdNewFile.addClickListener(event -> this.cmdNewFile_buttonClick(event));
		this.cmdDeleteFile.addClickListener(event -> this.cmdDeleteFile_buttonClick(event));
		this.cmdUpdateFile.addClickListener(event -> this.cmdUpdateFile_buttonClick(event));
		this.tableRowImage.addValueChangeListener(event -> this.tableRowImage_valueChange(event));
		this.cmdNewParam.addClickListener(event -> this.cmdNewParam_buttonClick(event));
		this.cmdDeleteParam.addClickListener(event -> this.cmdDeleteParam_buttonClick(event));
		this.cmdUpdateParam.addClickListener(event -> this.cmdUpdateParam_buttonClick(event));
		this.tableRowParam.addItemClickListener(event -> this.tableRowParam_itemClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevTable<RowParameter> tableRowParam;
	private XdevLabel lblObjId, lblObjAdded, lblObjDeleted, lblEntity, lblObjAddedBy, lblObjDeletedBy, lblObjRowId,
			lblObjChanged, lblDatabaseVersion, lblObjChngcnt, lblObjChangedBy, lblObjState;
	private XdevButton cmdNewText, cmdDeleteText, cmdUpdateText, cmdNewFile, cmdDeleteFile, cmdUpdateFile, cmdNewParam,
			cmdDeleteParam, cmdUpdateParam;
	private XdevComboBox<DatabaseVersion> cmbDatabaseVersion;
	private XdevTable<RowText> tableText;
	private XdevTable<RowImage> tableRowImage;
	private XdevPanel panel;
	private XdevTabSheet tabSheet;
	private XdevGridLayout form;
	private XdevFieldGroup<RowObject> fieldGroup;
	private XdevVerticalSplitPanel verticalSplitPanel;
	private XdevHorizontalLayout horizontalLayout2, actionLayout, actionLayoutParam;
	private XdevPopupDateField dateObjAdded, dateObjDeleted, dateObjChanged;
	private XdevTextField txtObjId, txtObjAddedBy, txtObjDeletedBy, txtObjRowId, txtObjChngcnt, txtObjChangedBy,
			txtObjState;
	private XdevComboBox<Entity> cmbEntity;
	private XdevVerticalLayout verticalLayoutText, verticalLayoutFile, verticalLayoutParam;
	// </generated-code>


}
