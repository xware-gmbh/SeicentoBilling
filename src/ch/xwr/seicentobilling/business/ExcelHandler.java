package ch.xwr.seicentobilling.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;

import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.ProjectLineDAO;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectLine;

public class ExcelHandler {
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ExcelHandler.class);

	/** the workbook */
	protected XSSFWorkbook hssfworkbook = null;

	private List<ProjectLine> PRList = null;


	public void importReportLine(final File file, final int sheetnbr, final Periode periode) {
		final String filename = file.getPath();
		//final POIFSFileSystem fs;
		InputStream fs1 = null;
		try {
			//fs = new POIFSFileSystem(new FileInputStream(filename));
			fs1 = new FileInputStream(filename);
			this.hssfworkbook = new XSSFWorkbook(fs1);

			//HSSFSheet sheet = null;
			XSSFSheet sheet = null;
			sheet = this.hssfworkbook.getSheetAt(sheetnbr);
			this.PRList = new ArrayList<>();

		    //final HSSFRow row = null;
		    XSSFRow row = null;
		    for (int i = 15; i < sheet.getLastRowNum(); i++) {
		    	row = sheet.getRow(i);
		    	loopExcelRow(row);
			}

		    persistList(periode);

		} catch (final FileNotFoundException e) {
			LOG.error("Bookkepping " + e);
		} catch (final IOException e) {
			LOG.error("Bookkepping " + e);
		} finally {
			closeFile(fs1);
		}


	}

	private void closeFile(final InputStream fs1) {

		try {
			fs1.close();
			this.hssfworkbook.close();
		} catch (final IOException e) {
			//ignore
		}

	}

	private void persistList(final Periode periode) {
		if (this.PRList == null || this.PRList.size() < 1) {
			return;
		}
		final ProjectLineDAO dao = new ProjectLineDAO();
		final RowObjectManager man = new RowObjectManager();

		for (final Iterator<ProjectLine> iterator = this.PRList.iterator(); iterator.hasNext();) {
			final ProjectLine bean = iterator.next();

			bean.setPeriode(periode);
			dao.save(bean);

			man.updateObject(bean.getPrlId(), bean.getClass().getSimpleName());
		}
	}

	private void loopExcelRow(final XSSFRow row) {
		if (row == null) {
			return;
		}
		XSSFCell cell;
		final ProjectLine bean = new ProjectLine();
		bean.setPrlState(LovState.State.active);
		bean.setPrlWorkType(LovState.WorkType.project);

		for (int i = 0; i < row.getLastCellNum(); i++) {
	        cell = row.getCell((short) i);
	        //final int type = cell.getCellType();

	        switch (i) {
			case 0:
				final String mandat = cell.getStringCellValue();
				if (mandat.isEmpty()) {
					return;
				}
				break;
			case 1:
				final String project = cell.getStringCellValue();
				final Project pro = findProject(project);
				bean.setProject(pro);
				break;
			case 2:
				final String code = cell.getStringCellValue();
				if ("FK".equals(code) || "FH".equals(code)) {
					bean.setPrlWorkType(LovState.WorkType.journey);
				}
				if ("SP".equals(code)) {
					bean.setPrlWorkType(LovState.WorkType.expense);
				}
				break;
			case 3:
				final Date date  = cell.getDateCellValue();
				bean.setPrlReportDate(date);
				break;

			case 4:
				final String text  = cell.getStringCellValue();
				bean.setPrlText(text);
				break;

			case 5:
				final double qty  = cell.getNumericCellValue();
				bean.setPrlHours(qty);
				break;
			case 6:
				final double amt  = cell.getNumericCellValue();
				bean.setPrlRate(amt);
				break;

			default:
				break;
			}
		}

		this.PRList.add(bean);
	}

	private Project findProject(final String project) {
		final ProjectDAO dao = new ProjectDAO();
		final Project bean = dao.findByName(project).get(0);
		return bean;
	}


//    protected HSSFCell getCellObj(final HSSFSheet sheet, final int irow, final String scol) {
//    	final int[] iref = getInternalCellRef(irow, scol);
//
//    	if (scol == "AD") {
//    		iref[1] = 29;
//    	}
//    	if (scol == "AC") {
//    		iref[1] = 28;
//    	}
//        final HSSFRow  row  = sheet.getRow(iref[0]);
//        final HSSFCell cell = row.getCell(iref[1], HSSFRow.CREATE_NULL_AS_BLANK);
//
//        return cell;
//	}
//
//    private int[] getInternalCellRef(final int irow, String scol) {
//    	final int[] ival = {0,0};
//    	int icol = 0;
//
//    	scol = scol.toUpperCase();
//    	icol = scol.charAt(0);
//    	icol = icol - 65;
//
//    	ival[0] = irow - 1;
//    	ival[1] = icol;
//
//		return ival;
//    }


}
