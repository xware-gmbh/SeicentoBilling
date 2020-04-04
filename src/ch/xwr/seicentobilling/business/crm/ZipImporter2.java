package ch.xwr.seicentobilling.business.crm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.log4j.LogManager;

import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload.ProgressListener;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.crm.model.ZipModel;
import ch.xwr.seicentobilling.dal.CityDAO;
import ch.xwr.seicentobilling.entities.City;

public final class ZipImporter2 extends Thread {
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(ZipImporter2.class);

	private String header[] = null;
	private String rowdata[] = null;
	int icount=0;
	int iNew = 0;
	int iUpdate = 0;

	private File inFile = null;
	private BufferedReader csvReader = null;
	private final CityDAO dao = new CityDAO();
    private LinkedHashSet<ProgressListener> progressListeners;

    //Tread-Stuff
    volatile double current = 0.0;
    private ProgressBar progress = new ProgressBar(new Float(0.0));
    private Label status = new Label();

    public ZipImporter2(final File inFile, final ProgressBar progress, final Label status) {
    	this.inFile = inFile;
    	this.progress = progress;
    	this.status = status;
    }

    @Override
    public void run() {
    	setReader();

        // Count up until 1.0 is reached

        // Do some "heavy work"
		String row = null;
		try {
			while ((row = readLine()) != null) {
	            this.current += 0.01;
				this.icount++;

				//fireUpdateProgress(this.icount, 200);
			    final String[] data = row.split(";");
				if (this.icount == 1) {
					this.header = data;
					LOG.debug("Header Line with " + this.header.length + " Fields.");
				} else {
					this.rowdata = data;
				    final ZipModel line = getZipModel();// do something with the data
				    if (isModelValid(line)) {
//				    	final WorkThread thread = new WorkThread(line);
//				    	thread.start();

					    upsertCity(line);

				    	this.iUpdate++;
				    } else {
				    	LOG.warn("Row " + this.icount + "is not valid");
				    }
				}

				//
			}
            try {
                sleep(50); // Sleep for 50 milliseconds
            } catch (final InterruptedException e) {}

            // Update the UI thread-safely
            UI.getCurrent().access(new Runnable() {
                @Override
                public void run() {
                	ZipImporter2.this.progress.setValue(new Float(ZipImporter2.this.current));
                    if (ZipImporter2.this.current < 1.0) {
                    	ZipImporter2.this.status.setValue("" +
                            ((int)(ZipImporter2.this.current*100)) + "% done");
					} else {
						ZipImporter2.this.status.setValue("all done");
					}
                }
            });
            closeReader();

	        try {
	            sleep(2000); // Sleep for 2 seconds
	        } catch (final InterruptedException e) {}

	        // Update the UI thread-safely
	        UI.getCurrent().access(new Runnable() {
	            @Override
	            public void run() {
	                // Restore the state to initial
	            	ZipImporter2.this.progress.setValue(new Float(0.0));
	            	ZipImporter2.this.progress.setEnabled(false);

	                // Stop polling
	                UI.getCurrent().setPollInterval(-1);

	                //button.setEnabled(true);
	                ZipImporter2.this.status.setValue("not running");
	            }
	        });

		} finally {
	        // Show the "all done" for a while
			closeReader();
		}
    }

    private String readLine() {
    	try {
			return this.csvReader.readLine();
		} catch (final IOException e) {
			return null;
		}
    }
	private void closeReader() {
		if (this.csvReader != null) {
			try {
				this.csvReader.close();
				this.csvReader = null;
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void setReader() {
		this.csvReader = null;
		if (!this.inFile.exists() || !this.inFile.canRead()) {
			LOG.error("File for City Import does not exist or can not be read " + this.inFile.getName());
		}

		LOG.info("Start processing file " + this.inFile.getName());
		//BufferedReader csvReader = null;
		try {
			this.csvReader = new BufferedReader(new FileReader(this.inFile));

		} catch (final FileNotFoundException e) {
			LOG.error("IO", e);
		}
	}

	public void readFile(final File fn) {
		if (!fn.exists() || !fn.canRead()) {
			LOG.error("File for City Import does not exist or can not be read " + fn.getName());
			return;
		}

		LOG.info("Start processing file " + fn.getName());
		BufferedReader csvReader = null;
		try {
			csvReader = new BufferedReader(new FileReader(fn));
			loopLines(csvReader);
			csvReader.close();

		} catch (final FileNotFoundException e) {
			LOG.error("IO", e);
		} catch (final IOException e) {
			LOG.error("IO", e);
		} finally {
			fn.delete();
		}

		LOG.info("[END] Number of lines processed " + this.icount + " New: " + this.iNew + " Updated: " + this.iUpdate);
	}

    public void addProgressListener(final ProgressListener listener) {
        if (this.progressListeners == null) {
            this.progressListeners = new LinkedHashSet<>();
        }
        this.progressListeners.add(listener);
    }

    protected void fireUpdateProgress(final long totalBytes, final long contentLength) {
        // this is implemented differently than other listeners to maintain
        // backwards compatibility
        if (this.progressListeners != null) {
            for (final Iterator<ProgressListener> it = this.progressListeners
                    .iterator(); it.hasNext();) {
                final ProgressListener l = it.next();
                l.updateProgress(totalBytes, contentLength);
            }
        }
    }

	public String getResultString() {
		return ("Number of lines processed " + this.icount + " New: " + this.iNew + " Updated: " + this.iUpdate);
	}

	private void loopLines(final BufferedReader csvReader) throws IOException {
		String row = "";
		while ((row = csvReader.readLine()) != null) {
			this.icount++;
			fireUpdateProgress(this.icount, 200);
		    final String[] data = row.split(";");
			if (this.icount == 1) {
				this.header = data;
				LOG.debug("Header Line with " + this.header.length + " Fields.");
			} else {
				this.rowdata = data;
			    final ZipModel line = getZipModel();// do something with the data
			    if (isModelValid(line)) {
				    //upsertCity(line);
			    	final WorkThread thread = new WorkThread(line);
			    	thread.start();
			    	this.iUpdate++;
			    } else {
			    	LOG.warn("Row " + this.icount + "is not valid");
			    }
			}
		}
	}

	private boolean isModelValid(final ZipModel line) {
		final Date today = new Date();

		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			if (! line.getGilt_ab_date().isEmpty()) {
				final Date vfrom = df.parse(line.getGilt_ab_date());
				if (vfrom.after(today)) {
					return false;
				}
			}
		} catch (final ParseException e) {
			LOG.warn("Could not parse valid date " + line.getGilt_ab_date());
		}

		return true;
	}

	private void upsertCity(final ZipModel line) {
		City bean = null;

		final List<City> ls = this.dao.findByZip(line.getPostleitzahl());
		if (ls == null || ls.isEmpty()) {
			bean = getNewBean(line);
			this.iNew++;
		} else {
			bean = updateBean(ls.get(0), line);  //first bean
			this.iUpdate++;
		}
		this.dao.save(bean);
		LOG.debug("Record written to Database with id: " +  bean.getCtyId());

	}

	private City updateBean(final City bean, final ZipModel line) {
		bean.setCtyCountry("CH");
		bean.setCtyGeoCoordinates(line.getGeo_point_2d());
		if (bean.getCtyGeoCoordinates().length() > 20) {
			bean.setCtyGeoCoordinates(bean.getCtyGeoCoordinates().substring(0, 19));
		}
		bean.setCtyName(line.getOrtbez27());
		bean.setCtyRegion(line.getKanton());
		bean.setCtyState(LovState.State.active);
		bean.setCtyZip(line.getPostleitzahl());

		return bean;
	}

	private City getNewBean(final ZipModel line) {
		final City bean = new City();
		return updateBean(bean, line);
	}

	private ZipModel getZipModel() {
		final ZipModel zip = new ZipModel();

		zip.setBfsnr(Integer.parseInt(getField(ZipModel.BFSNR)));
		zip.setGilt_ab_date(getField((ZipModel.VALID_FROM)));
		zip.setKanton(getField(ZipModel.AREA));
		zip.setOrtbez27(getField(ZipModel.CITY27));
		zip.setPlz_typ(Integer.parseInt(getField(ZipModel.ZIP_TYP)));
		zip.setPostleitzahl(Integer.parseInt(getField(ZipModel.ZIP)));
		zip.setRec_art(Integer.parseInt(getField(ZipModel.REC_ART)));
		zip.setGeo_point_2d(getField(ZipModel.GEO_POINT));

		return zip;
	}

	private String getField(final String fieldname) {
		final int idx = getIndex(fieldname);
		if (this.rowdata.length < idx) {
			return "";
		}
		return this.rowdata[idx];
	}


	private int getIndex(final String key) {
		for (int i = 0; i < this.header.length; i++) {
			if (key.equalsIgnoreCase(this.header[i])) {
				return i;
			}
		}
		LOG.error("Field " + key + " not found in csv File");
		return -1;
	}

}


class WorkThread extends Thread {
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(WorkThread.class);
	private ZipModel line = null;

	public WorkThread(final ZipModel line) {
		this.line = line;
	}

    @Override
    public void run() {
		City bean = null;
		final CityDAO dao = new CityDAO();

		final List<City> ls = dao.findByZip(this.line.getPostleitzahl());
		if (ls == null || ls.isEmpty()) {
			bean = getNewBean(this.line);
		} else {
			bean = updateBean(ls.get(0), this.line);  //first bean
		}
		dao.save(bean);
		LOG.debug("Record written to Database with id: " +  bean.getCtyId());
    }

	private City getNewBean(final ZipModel line) {
		final City bean = new City();
		return updateBean(bean, line);
	}

	private City updateBean(final City bean, final ZipModel line) {
		bean.setCtyCountry("CH");
		bean.setCtyGeoCoordinates(line.getGeo_point_2d());
		if (bean.getCtyGeoCoordinates().length() > 20) {
			bean.setCtyGeoCoordinates(bean.getCtyGeoCoordinates().substring(0, 19));
		}
		bean.setCtyName(line.getOrtbez27());
		bean.setCtyRegion(line.getKanton());
		bean.setCtyState(LovState.State.active);
		bean.setCtyZip(line.getPostleitzahl());

		return bean;
	}

}