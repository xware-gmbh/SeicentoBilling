
package ch.xwr.seicentobilling.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.PersistenceException;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.util.Log;

import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.ProjectLineDAO;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectLine;


/**
 * handels (import) Excel with reporting lines
 */
public class ExcelHandler
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ExcelHandler.class);
	
	/** the workbook */
	protected XSSFWorkbook hssfworkbook = null;
	
	private List<ProjectLine>          PRList   = null;
	private HashMap<Long, ProjectLine> projects = null;
	
	private int iTotRead  = 0;
	private int iTotSaved = 0;
	
	private String sheetName = "";
	
	public void importReportLine(final File file, final int sheetnbr, final Periode periode) throws Exception
	{
		final String filename = file.getPath();
		// final POIFSFileSystem fs;
		InputStream fs1 = null;
		try
		{
			// fs = new POIFSFileSystem(new FileInputStream(filename));
			ExcelHandler.LOG.info("Open File " + filename + " for processing");
			fs1               = new FileInputStream(filename);
			this.hssfworkbook = new XSSFWorkbook(fs1);
			
			// HSSFSheet sheet = null;
			XSSFSheet sheet = null;
			sheet          = this.hssfworkbook.getSheetAt(sheetnbr);
			this.sheetName = this.hssfworkbook.getSheetName(sheetnbr);
			this.PRList    = new ArrayList<>();
			ExcelHandler.LOG.info("select sheet Nbr " + sheetnbr + " " + this.sheetName);
			
			// final HSSFRow row = null;
			XSSFRow row = null;
			for(int i = 15; i < sheet.getLastRowNum(); i++)
			{
				ExcelHandler.LOG.info("Start processing Excel-line " + (i + 1));
				this.iTotRead++;
				row = sheet.getRow(i);
				try
				{
					this.loopExcelRow(row);
				}
				catch(final Exception e)
				{
					ExcelHandler.LOG.error("Error on Line: " + (i + 1));
					ExcelHandler.LOG.error(e.getMessage());
					
					throw new Exception(
						"Zeile " + (i + 1) + " konnte nicht verarbeitet werden. " + e.getLocalizedMessage());
				}
			}
			
			this.persistList(periode);
			this.recalcProjects();
			
		}
		catch(final FileNotFoundException e)
		{
			ExcelHandler.LOG.error("Excel " + e);
		}
		catch(final IOException e)
		{
			ExcelHandler.LOG.error("Excel " + e);
		}
		finally
		{
			this.closeFile(fs1);
			fs1 = null;
		}
		
	}
	
	private void recalcProjects()
	{
		ExcelHandler.LOG.debug("start recalculating hours on relevant projects");
		if(this.projects == null || this.projects.isEmpty())
		{
			return;
		}
		
		final ProjectDAO dao = new ProjectDAO();
		for(final Long proId : this.projects.keySet())
		{
			dao.calculateEffectiveHours(proId);
		}
		
	}
	
	private void closeFile(final InputStream fs1)
	{
		
		try
		{
			fs1.close();
			this.hssfworkbook.close();
		}
		catch(final IOException e)
		{
			// ignore
		}
	}
	
	private void persistList(final Periode periode) throws Exception
	{
		if(this.PRList == null || this.PRList.size() < 1)
		{
			ExcelHandler.LOG.warn("No valid entry in List to save to ProjectLine!");
			return;
		}
		ExcelHandler.LOG.info("Try to save: " + this.PRList.size() + " valid entries from excel!");
		// Creating HashMap
		this.projects = new HashMap<>();
		final ProjectLineDAO   dao = new ProjectLineDAO();
		final RowObjectManager man = new RowObjectManager();
		
		try
		{
			dao.disableTrigger(true);
			Log.debug("disabled Trigger on ProjectLine");
			
			for(final Iterator<ProjectLine> iterator = this.PRList.iterator(); iterator.hasNext();)
			{
				final ProjectLine bean = iterator.next();
				
				this.checkValidDate(bean, periode);
				
				bean.setPeriode(periode);
				dao.save(bean);
				ExcelHandler.LOG.debug("saved record with id: " + bean.getPrlId() + " Projekt: "
					+ bean.getProject().getProName() + " KST: " + bean.getPeriode().getPerName());
				this.iTotSaved++;
				
				man.updateObject(bean.getPrlId(), bean.getClass().getSimpleName());
				if(!this.projects.containsKey(bean.getProject().getProId()))
				{
					this.projects.put(bean.getProject().getProId(), bean);
				}
				// Thread.sleep(100); //give DB trigger some time
				// reread(bean);
			}
			dao.flush();
		}
		catch(final PersistenceException cx)
		{
			String msg = cx.getMessage();
			if(cx.getCause() != null)
			{
				msg = cx.getCause().getMessage();
				if(cx.getCause().getCause() != null)
				{
					msg = cx.getCause().getCause().getMessage();
				}
			}
			ExcelHandler.LOG.error(msg);
		}
		finally
		{
			dao.disableTrigger(false);
			Log.debug("enabled Trigger on ProjectLine");
		}
		
	}
	
	// private void reread(final ProjectLine bean) {
	// final ProjectLineDAO dao = new ProjectLineDAO();
	// final ProjectLine rbean = dao.find(bean.getPrlId());
	// if (rbean.getPrlId() != bean.getPrlId()) {
	// LOG.warn("id's are not the same in reread");
	// }
	// }
	
	private void checkValidDate(final ProjectLine bean, final Periode periode) throws Exception
	{
		// Rapportdatum muss zu Periode passen
		final Calendar cal = Calendar.getInstance();
		cal.setTime(bean.getPrlReportDate());
		final int imonth = cal.get(Calendar.MONTH) + 1;
		
		if(imonth != periode.getPerMonth().getValue())
		{
			throw new Exception("Periode " + periode.getPerName() + " nicht gültig für Sheet " + this.sheetName
				+ " und Rapport-Datum: " + bean.getPrlReportDate());
		}
	}
	
	private void loopExcelRow(final XSSFRow row) throws Exception
	{
		if(row == null)
		{
			return;
		}
		XSSFCell          cell;
		final ProjectLine bean = new ProjectLine();
		bean.setPrlState(LovState.State.active);
		bean.setPrlWorkType(LovState.WorkType.project);
		
		for(int i = 0; i < row.getLastCellNum(); i++)
		{
			cell = row.getCell((short)i);
			if(cell == null)
			{
				if(i == 0)
				{
					return;
				}
				else
				{
					continue;
				}
			}
			// final int type = cell.getCellType();
			
			switch(i)
			{
				case 0:
					final String mandat = cell.getStringCellValue();
					if(mandat == null || mandat.isEmpty())
					{
						return;
					}
				break;
				case 1:
					final String project = cell.getStringCellValue();
					final Project pro = this.findProject(project);
					bean.setProject(pro);
				break;
				case 2:
					final String code = cell.getStringCellValue();
					if("FK".equals(code) || "FH".equals(code))
					{
						bean.setPrlWorkType(LovState.WorkType.journey);
					}
					if("SP".equals(code))
					{
						bean.setPrlWorkType(LovState.WorkType.expense);
					}
				break;
				case 3:
					final Date date = cell.getDateCellValue();
					bean.setPrlReportDate(date);
				break;
			
				case 4:
					final String text = cell.getStringCellValue();
					bean.setPrlText(text);
				break;
			
				case 5:
					final double qty = cell.getNumericCellValue();
					bean.setPrlHours(qty);
				break;
				case 6:
					final double amt = cell.getNumericCellValue();
					bean.setPrlRate(amt);
				break;
			
				default:
				break;
			}
		}
		
		if(bean.getProject() != null && bean.getPrlReportDate() != null && bean.getPrlText().length() > 0)
		{
			this.PRList.add(bean);
		}
		else
		{
			ExcelHandler.LOG.debug("ignoring current line. Will not be added to list.");
		}
	}
	
	private Project findProject(final String project) throws Exception
	{
		if(project.equals("*"))
		{
			return null; // ignore line
		}
		final ProjectDAO dao = new ProjectDAO();
		try
		{
			final Project bean = dao.findEqNameIgnoreCase(project).get(0);
			return bean;
		}
		catch(final Exception e)
		{
			throw new Exception("Projekt nicht gefunden '" + project + "'");
		}
	}
	
	public String getResultString()
	{
		return "Total Zeilen gelesen " + this.sheetName + ": " + this.iTotRead + "  Total Records gespeichert: "
			+ this.iTotSaved;
	}
	
	// protected HSSFCell getCellObj(final HSSFSheet sheet, final int irow, final String scol) {
	// final int[] iref = getInternalCellRef(irow, scol);
	//
	// if (scol == "AD") {
	// iref[1] = 29;
	// }
	// if (scol == "AC") {
	// iref[1] = 28;
	// }
	// final HSSFRow row = sheet.getRow(iref[0]);
	// final HSSFCell cell = row.getCell(iref[1], HSSFRow.CREATE_NULL_AS_BLANK);
	//
	// return cell;
	// }
	//
	// private int[] getInternalCellRef(final int irow, String scol) {
	// final int[] ival = {0,0};
	// int icol = 0;
	//
	// scol = scol.toUpperCase();
	// icol = scol.charAt(0);
	// icol = icol - 65;
	//
	// ival[0] = irow - 1;
	// ival[1] = icol;
	//
	// return ival;
	// }
	
}
