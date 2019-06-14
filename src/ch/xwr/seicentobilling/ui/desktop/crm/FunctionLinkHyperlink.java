
package ch.xwr.seicentobilling.ui.desktop.crm;

import java.net.MalformedURLException;
import java.net.URL;

import org.jfree.util.Log;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLink;
import com.xdev.ui.entitycomponent.table.XdevTable;

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.entities.CustomerLink;

public class FunctionLinkHyperlink extends XdevHorizontalLayout {

	public static class Generator implements ColumnGenerator {
		@Override
		public Object generateCell(final Table table, final Object itemId, final Object columnId) {

			return new FunctionLinkHyperlink(table, itemId, columnId);
		}
	}

	private final Table customizedTable;
	private final Object itemId;
	private final Object columnId;

	private FunctionLinkHyperlink(final Table customizedTable, final Object itemId, final Object columnId) {
		super();

		this.customizedTable = customizedTable;
		this.itemId = itemId;
		this.columnId = columnId;

		this.initUI();

		this.link.setDescription(this.getBean().getCnkLink());
		setIcon(this.getBean());

		final String link = getUrlLink(getBean());
		try {
			this.link.setResource(new ExternalResource(new URL(link)));
		} catch (final MalformedURLException e) {
			Log.error(e);
		}
	}

	private void setIcon(final CustomerLink bean) {
		if (this.getBean().getCnkLink() != null) {
			if (this.getBean().getCnkLink().isEmpty()) {
				this.link.setEnabled(false);
			}

			if (this.getBean().getCnkType() == LovCrm.LinkType.phone) {
				this.link.setIcon(FontAwesome.PHONE);

			}
			if (this.getBean().getCnkType() == LovCrm.LinkType.mail) {
				this.link.setIcon(FontAwesome.ENVELOPE);
			}
		} else {
			this.link.setEnabled(false);
		}
	}

	public Table getTable() {
		return this.customizedTable;
	}

	public Object getItemId() {
		return this.itemId;
	}

	public Object getColumnId() {
		return this.columnId;
	}

	@SuppressWarnings("unchecked")
	public CustomerLink getBean() {
		return ((XdevTable<CustomerLink>) getTable()).getBeanContainerDataSource().getItem(getItemId()).getBean();
	}

	private String getUrlLink(final CustomerLink act) {
		String url = "";

		if (act.getCnkType() == LovCrm.LinkType.phone) {
			url = getTelLink(act);
		} else if (act.getCnkType() == LovCrm.LinkType.mail) {
			url = getMailLink(act);
		} else {
			url = getWebLink(act.getCnkLink());
		}

		return url;
	}

//	private void launchLink() {
//		final CustomerLink act = getBean();
//		String url = "";
//
//		if (act.getCnkType() == LovCrm.LinkType.phone) {
//			url = getTelLink(act);
//		} else if (act.getCnkType() == LovCrm.LinkType.mail) {
//			url = getMailLink(act);
//		} else {
//			url = act.getCnkLink();
//		}
//
//		try {
//	        if (Desktop.isDesktopSupported()) {
//	            // Windows
//	            Desktop.getDesktop().browse(new URI(url));
//	        } else {
//	            // Ubuntu
//	            final Runtime runtime = Runtime.getRuntime();
//	            runtime.exec("/usr/bin/firefox -new-window " + url);
//	        }
//
//		} catch (final Exception e) {
//
//		}
//	}

	private String getWebLink(String cnkLink) {
		try {
			final URL myUrl = new URL(cnkLink);
			return myUrl.toString();
		} catch (final MalformedURLException e) {
			Log.error(e);
		}

		cnkLink = "https://" + cnkLink;
		return cnkLink;
	}

	private String getTelLink(final CustomerLink act) {
		//für tel: gibt es ein ProtocolExtender.jar welches im java bzw. docker build ist.
		if (!act.getCnkLink().isEmpty()) {
			final String st = act.getCnkLink().replaceAll("\\s+", "");
			return "tel:" + st;
		}
		return "";
	}

	private String getMailLink(final CustomerLink act) {
		if (!act.getCnkLink().isEmpty()) {
			final String st = act.getCnkLink().replaceAll("\\s+", "");
			return "mailto:" + st;
		}
		return "";
	}


	/**
	 * Event handler delegate method for the {@link XdevHorizontalLayout}.
	 *
	 * @see LayoutClickListener#layoutClick(LayoutClickEvent)
	 * @eventHandlerDelegate
	 */
	private void this_layoutClick(final LayoutClickEvent event) {
		selectItem();
	}

	private void selectItem() {
		getTable().select(getItemId());
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.link = new XdevLink();

		this.setSpacing(false);
		this.setMargin(new MarginInfo(false));
		this.link.setTargetName("_blank");
		this.link.setIcon(FontAwesome.BOLT);
		this.link.setCaption("Link");

		this.link.setSizeUndefined();
		this.addComponent(this.link);
		this.setComponentAlignment(this.link, Alignment.MIDDLE_CENTER);
		this.setSizeUndefined();

		this.addLayoutClickListener(event -> this.this_layoutClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLink link;
	// </generated-code>


}
