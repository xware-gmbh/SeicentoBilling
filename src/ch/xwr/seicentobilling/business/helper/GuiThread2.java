package ch.xwr.seicentobilling.business.helper;

import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;

public class GuiThread2 extends Thread {
    // Volatile because read in another thread in access()
    volatile double current = 0.0;
    public ProgressBar progress = new ProgressBar(new Float(0.0));
    public Label status = new Label();

    @Override
    public void run() {
        // Count up until 1.0 is reached
        while (this.current < 1.0) {
            this.current += 0.01;

            // Do some "heavy work"
            try {
                sleep(50); // Sleep for 50 milliseconds
            } catch (final InterruptedException e) {}

            // Update the UI thread-safely
            UI.getCurrent().access(new Runnable() {
                @Override
                public void run() {
                	GuiThread2.this.progress.setValue(new Float(GuiThread2.this.current));
                    if (GuiThread2.this.current < 1.0) {
                    	GuiThread2.this.status.setValue("" +
                            ((int)(GuiThread2.this.current*100)) + "% done");
					} else {
						GuiThread2.this.status.setValue("all done");
					}
                }
            });
        }

        // Show the "all done" for a while
        try {
            sleep(2000); // Sleep for 2 seconds
        } catch (final InterruptedException e) {}

        // Update the UI thread-safely
        UI.getCurrent().access(new Runnable() {
            @Override
            public void run() {
                // Restore the state to initial
            	GuiThread2.this.progress.setValue(new Float(0.0));
            	GuiThread2.this.progress.setEnabled(false);

                // Stop polling
                UI.getCurrent().setPollInterval(-1);

                //button.setEnabled(true);
                GuiThread2.this.status.setValue("not running");
            }
        });
    }
}

