
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
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.apache.log4j.LogManager;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.model.crm.ZipModel;
import ch.xwr.seicentobilling.dal.CityDAO;
import ch.xwr.seicentobilling.entities.City;


public final class ZipImporter
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(ZipImporter.class);
	
	private String header[]  = null;
	private String rowdata[] = null;
	int            icount    = 0;
	int            iNew      = 0;
	int            iUpdate   = 0;
	
	private CityDAO dao;
	// private LinkedHashSet<ProgressListener> progressListeners;
	
	@PersistenceContext(unitName = "City", type = PersistenceContextType.EXTENDED)
	private EntityManager entityManager;
	
	public void readFile(final File fn)
	{
		if(!fn.exists() || !fn.canRead())
		{
			ZipImporter.LOG.error("File for City Import does not exist or can not be read " + fn.getName());
			return;
		}
		this.dao = new CityDAO();
		
		ZipImporter.LOG.info("Start processing file " + fn.getName());
		BufferedReader csvReader = null;
		try
		{
			csvReader = new BufferedReader(new FileReader(fn));
			this.loopLines(csvReader);
			csvReader.close();
			
			this.dao.flush();
			this.dao = null;
			
		}
		catch(final FileNotFoundException e)
		{
			ZipImporter.LOG.error("IO", e);
		}
		catch(final IOException e)
		{
			ZipImporter.LOG.error("IO", e);
		}
		finally
		{
			fn.delete();
		}
		
		ZipImporter.LOG.info(
			"[END] Number of lines processed " + this.icount + " New: " + this.iNew + " Updated: " + this.iUpdate);
	}
	
	// public void addProgressListener(final ProgressListener listener) {
	// if (this.progressListeners == null) {
	// this.progressListeners = new LinkedHashSet<>();
	// }
	// this.progressListeners.add(listener);
	// }
	
	// protected void fireUpdateProgress(final long totalBytes, final long contentLength) {
	// // this is implemented differently than other listeners to maintain
	// // backwards compatibility
	// if (this.progressListeners != null) {
	// for (final Iterator<ProgressListener> it = this.progressListeners
	// .iterator(); it.hasNext();) {
	// final ProgressListener l = it.next();
	// l.updateProgress(totalBytes, contentLength);
	// }
	// }
	// }
	
	public String getResultString()
	{
		return ("Number of lines processed " + this.icount + " New: " + this.iNew + " Updated: " + this.iUpdate);
	}
	
	private void loopLines(final BufferedReader csvReader) throws IOException
	{
		String row = "";
		while((row = csvReader.readLine()) != null)
		{
			this.icount++;
			// fireUpdateProgress(this.icount, 200);
			final String[] data = row.split(";");
			if(this.icount == 1)
			{
				this.header = data;
				ZipImporter.LOG.debug("Header Line with " + this.header.length + " Fields.");
			}
			else
			{
				this.rowdata = data;
				final ZipModel line = this.getZipModel();// do something with the data
				if(this.isModelValid(line))
				{
					this.upsertCity(line);
				}
				else
				{
					ZipImporter.LOG.warn("Row " + this.icount + "is not valid");
				}
			}
		}
	}
	
	private boolean isModelValid(final ZipModel line)
	{
		final Date today = new Date();
		
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try
		{
			if(!line.getGilt_ab_date().isEmpty())
			{
				final Date vfrom = df.parse(line.getGilt_ab_date());
				if(vfrom.after(today))
				{
					return false;
				}
			}
		}
		catch(final ParseException e)
		{
			ZipImporter.LOG.warn("Could not parse valid date " + line.getGilt_ab_date());
		}
		if(line.getPlz_coff() == null || line.getPlz_coff().trim().isEmpty())
		{
			return false; // skip
		}
		
		return true;
	}
	
	private void upsertCity(final ZipModel line)
	{
		City bean = null;
		
		final List<City> ls = this.dao.findByZip(line.getPostleitzahl());
		if(ls == null || ls.isEmpty())
		{
			bean = this.getNewBean(line);
			this.iNew++;
		}
		else
		{
			bean = this.updateBean(ls.get(0), line); // first bean
			this.iUpdate++;
		}
		// this.dao.save(bean);
		this.dao.persist(bean);
		
		if(this.icount % 10 == 0)
		{
			ZipImporter.LOG.debug(
				"Record #" + (this.icount - 1) + " written to Database. New: " + this.iNew + " Update " + this.iUpdate);
		}
		if(this.icount % 100 == 0)
		{
			this.dao.flush();
		}
	}
	
	private City updateBean(final City bean, final ZipModel line)
	{
		bean.setCtyCountry("CH");
		bean.setCtyGeoCoordinates(line.getGeo_point_2d());
		if(bean.getCtyGeoCoordinates().length() > 20)
		{
			bean.setCtyGeoCoordinates(bean.getCtyGeoCoordinates().substring(0, 19));
		}
		bean.setCtyName(line.getOrtbez27());
		bean.setCtyRegion(line.getKanton());
		bean.setCtyState(LovState.State.active);
		bean.setCtyZip(line.getPostleitzahl());
		
		return bean;
	}
	
	private City getNewBean(final ZipModel line)
	{
		final City bean = new City();
		return this.updateBean(bean, line);
	}
	
	private ZipModel getZipModel()
	{
		final ZipModel zip = new ZipModel();
		
		zip.setBfsnr(Integer.parseInt(this.getField(ZipModel.BFSNR)));
		zip.setGilt_ab_date(this.getField((ZipModel.VALID_FROM)));
		zip.setKanton(this.getField(ZipModel.AREA));
		zip.setOrtbez27(this.getField(ZipModel.CITY27));
		zip.setPlz_typ(Integer.parseInt(this.getField(ZipModel.ZIP_TYP)));
		zip.setPostleitzahl(Integer.parseInt(this.getField(ZipModel.ZIP)));
		zip.setRec_art(Integer.parseInt(this.getField(ZipModel.REC_ART)));
		zip.setGeo_point_2d(this.getField(ZipModel.GEO_POINT));
		zip.setPlz_zz(Integer.parseInt(this.getField(ZipModel.PLZ_ZZ)));
		zip.setPlz_coff(this.getField(ZipModel.PLZ_COFF));
		
		return zip;
	}
	
	private String getField(final String fieldname)
	{
		final int idx = this.getIndex(fieldname);
		if(this.rowdata.length <= idx)
		{
			return "";
		}
		return this.rowdata[idx];
	}
	
	private int getIndex(final String key)
	{
		for(int i = 0; i < this.header.length; i++)
		{
			if(key.equalsIgnoreCase(this.header[i]))
			{
				return i;
			}
		}
		ZipImporter.LOG.error("Field " + key + " not found in csv File");
		return -1;
	}
	
}
