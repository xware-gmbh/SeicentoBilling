package ch.xwr.seicentobilling.business.helper;

import java.io.File;

public class FileUploadDto {
	private String subject = "";
	private String filter = "*";
	private File upfile = null;
	private boolean success = false;
	private String message = "";
	private long size = 0;

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

}
