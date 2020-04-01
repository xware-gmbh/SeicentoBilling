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

import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.crm.model.ZipModel;
import ch.xwr.seicentobilling.dal.CityDAO;
import ch.xwr.seicentobilling.entities.City;

public final class ZipImporter {
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ZipImporter.class);
	private String header[] = null;
	private String rowdata[] = null;

	private final CityDAO dao = new CityDAO();

	public void readFile(final File fn) {
		BufferedReader csvReader = null;
		try {
			csvReader = new BufferedReader(new FileReader(fn));
			loopLines(csvReader);
			csvReader.close();
		} catch (final FileNotFoundException e) {
			LOG.error("IO", e);
		} catch (final IOException e) {
			LOG.error("IO", e);
		}
	}

	private void loopLines(final BufferedReader csvReader) throws IOException {
		String row = "";
		int icount=0;
		while ((row = csvReader.readLine()) != null) {
			icount++;
		    final String[] data = row.split(";");
			if (icount == 1) {
				this.header = data;
				LOG.debug("Header Line with " + this.header.length + " Fields.");
			} else {
				this.rowdata = data;
			    final ZipModel line = getZipModel();// do something with the data
			    if (isModelValid(line)) {
				    upsertCity(line);
			    } else {
			    	LOG.warn("Row " + icount + "is not valid");
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
		} else {
			bean = updateBean(ls.get(0), line);  //first bean
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
