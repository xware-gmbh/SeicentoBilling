
package ch.xwr.seicentobilling.ui;

import java.io.ByteArrayInputStream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;

import ch.xwr.seicentobilling.entities.RowImage;


public class FunctionUpDownloadRowFile
{
	public Anchor createDownLoadButton(final Grid<RowImage> grid, final RowImage rowFile)
	{
		
		Anchor cmdDownload = new Anchor("");

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
	
}
