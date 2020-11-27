package ch.xwr.seicentobilling.business.svc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.RowImageDAO;
import ch.xwr.seicentobilling.dal.RowObjectDAO;
import ch.xwr.seicentobilling.entities.RowImage;
import ch.xwr.seicentobilling.entities.RowObject;

public class SaveFileToDb {
	private RowObject rowobj = null;
	private RowImageDAO dao = null;
	private RowImage bean = null;
	private File inf = null;

	public SaveFileToDb(final String filename, final String mimeType, final long objId) {
		this.dao = new RowImageDAO();
		this.rowobj = new RowObjectDAO().find(objId);

		this.inf = new File(filename);
		this.bean = getRowImageBean();

		this.bean.setRimMimetype(mimeType);
		this.bean.setRimName(this.inf.getName());
	}

	public void importFile() {
		try {
			this.bean.setRimImage(Files.readAllBytes(Paths.get(this.inf.getAbsolutePath())));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		this.bean.setRimSize(this.inf.length() + " Bytes");
		this.dao.save(this.bean);

		this.inf.delete();
	}


	private RowImage getRowImageBean() {
		RowImage bean = null;

		if (this.rowobj != null && this.rowobj.getObjId() > 0) {
			final int nextNbr = getNextRimNumber();

			bean = new RowImage();
			bean.setRimState(LovState.State.active);
			bean.setRowObject(this.rowobj);
			bean.setRimNumber(nextNbr);
			bean.setRimType((short) 2);
		}
		return bean;
	}

	private int getNextRimNumber() {
	    final List<RowImage> lst = this.dao.findByObject(this.rowobj);
	    if (lst == null || lst.isEmpty()) {
	    	return 850;
	    }

	    int inbr = 0;
	    for (final RowImage rowImage : lst) {
			if (rowImage.getRimNumber() > inbr) {
				inbr = rowImage.getRimNumber();
			}
		}
	    if (inbr < 850) {
	    	return 850;
	    }
		return inbr + 10;
	}

}
