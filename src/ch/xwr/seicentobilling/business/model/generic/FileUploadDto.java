package ch.xwr.seicentobilling.business.model.generic;

import java.io.File;

public class FileUploadDto {
	private String subject = "";
	private String filter = "*";
	private File upfile = null;
	private boolean success = false;
	private String message = "";
	private long size = 0;
	private long maxSize = (45 * 1024 * 1024);  //max size 40 MB;

	public String getSubject() {
		return this.subject;
	}
	public void setSubject(final String subject) {
		this.subject = subject;
	}
	public String getFilter() {
		return this.filter;
	}
	public void setFilter(final String filter) {
		this.filter = filter;
	}
	public File getUpfile() {
		return this.upfile;
	}
	public void setUpfile(final File upfile) {
		this.upfile = upfile;
	}
	public boolean isSuccess() {
		return this.success;
	}
	public void setSuccess(final boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return this.message;
	}
	public void setMessage(final String message) {
		this.message = message;
	}
	public long getSize() {
		return this.size;
	}
	public void setSize(final long size) {
		this.size = size;
	}
	public long getMaxSize() {
		return this.maxSize;
	}
	public void setMaxSize(final long maxSize) {
		this.maxSize = maxSize;
	}

}
