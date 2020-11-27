
package ch.xwr.seicentobilling.business.helper;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.Command;


public class GuiThread2 extends Thread
{
	// Volatile because read in another thread in access()
	volatile double    current  = 0.0;
	public ProgressBar progress = new ProgressBar();
	public Label       status   = new Label();
	
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
					GuiThread2.this.progress.setValue(new Float(GuiThread2.this.current));
					if(GuiThread2.this.current < 1.0)
					{
						GuiThread2.this.status.setText("" +
							((int)(GuiThread2.this.current * 100)) + "% done");
					}
					else
					{
						GuiThread2.this.status.setText("all done");
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
				GuiThread2.this.progress.setValue(new Float(0.0));
				GuiThread2.this.progress.setVisible(false);
				
				// Stop polling
				UI.getCurrent().setPollInterval(-1);
				
				// button.setEnabled(true);
				GuiThread2.this.status.setText("not running");
			}
		});
	}
}
