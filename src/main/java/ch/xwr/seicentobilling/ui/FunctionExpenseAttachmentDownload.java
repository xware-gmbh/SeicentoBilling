
package ch.xwr.seicentobilling.ui;

import java.io.ByteArrayInputStream;
import java.util.Set;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;

import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.RowImage;
import ch.xwr.seicentobilling.entities.RowObject;


public class FunctionExpenseAttachmentDownload
{
	public Anchor createDownLoadButton(final Grid<Expense> grid, final Expense bean)
	{
		final RowImage rowFile     = this.getBean(bean);
		Anchor         cmdDownload = new Anchor("");

		cmdDownload.setTitle("Download attachment");
		final Button db = new Button(new Icon(VaadinIcon.DOWNLOAD));
		
		if(rowFile != null)
		{

			cmdDownload =
				new Anchor(new StreamResource(rowFile.getRimName(), () -> {
					return new ByteArrayInputStream(rowFile.getRimImage());
				}),
					"");

			cmdDownload.setTitle("Download " + rowFile.getRimName());
			cmdDownload.getElement().setAttribute("download", true);
			db.addClickListener(ee -> {
				SeicentoNotification.showInfo("Download gestartet für: " + rowFile.getRimName());
				
			});
		}
		else
		{
			cmdDownload.setEnabled(false);
		}
		cmdDownload.add(db);
		return cmdDownload;
	}
	
	public RowImage getBean(final Expense exp)
	{
		RowImage img = null;
		
		final RowObjectManager man = new RowObjectManager();
		final RowObject        obj = man.getRowObject(exp.getClass().getSimpleName(), exp.getExpId());

		if(obj != null)
		{
			final Set<RowImage> ls = obj.getRowImages();
			if(ls != null && ls.size() > 0)
			{
				img = ls.iterator().next();
			}
		}

		return img;
	}
}
