
package ch.xwr.seicentobilling.business.helper;

import java.io.File;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.Command;


public class GuiThread extends Thread
{
	// Volatile because read in another thread in access()
	volatile double current = 0.0;

	// myStuff
	private ProgressBar progress = new ProgressBar();
	private Label       status   = new Label();
	private File        inFile   = null;

	public GuiThread(final File inFile, final ProgressBar progress, final Label status)
	{
		this.inFile   = inFile;
		this.progress = progress;
		this.status   = status;
	}

	@Override
	public void run()
	{
		// Count up until 1.0 is reached
		while(this.current < 1.0)
		{
			this.current += 0.01;

			// Do some "heavy work"
			try
			{
				Thread.sleep(50); // Sleep for 50 milliseconds
			}
			catch(final InterruptedException e)
			{
			}

			// Update the UI thread-safely
			UI.getCurrent().access(new Command()
			{

				@Override
				public void execute()
				{
					GuiThread.this.progress.setValue(new Float(GuiThread.this.current));
					if(GuiThread.this.current < 1.0)
					{
						GuiThread.this.status.setText("" +
							((int)(GuiThread.this.current * 100)) + "% done");
					}
					else
					{
						GuiThread.this.status.setText("all done");
					}

				}
			});
		}

		// Show the "all done" for a while
		try
		{
			Thread.sleep(2000); // Sleep for 2 seconds
		}
		catch(final InterruptedException e)
		{
		}

		// Update the UI thread-safely
		UI.getCurrent().access(new Command()
		{
			@Override
			public void execute()
			{
				// Restore the state to initial
				GuiThread.this.progress.setValue(new Float(0.0));
				GuiThread.this.progress.setVisible(false);

				// Stop polling
				UI.getCurrent().setPollInterval(-1);

				// button.setEnabled(true);
				GuiThread.this.status.setText("not running");
			}
		});
	}
}
