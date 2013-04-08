package org.chaosfisch.youtubeuploader.vo;

import org.chaosfisch.util.GsonHelper;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;

public class UploadViewVO {
	private Template	template;
	private Upload		upload;

	private Template	backupTemplate;
	private Upload		backupUpload;

	/**
	 * @return the template
	 */
	public Template getTemplate() {
		return template;
	}

	/**
	 * @return the upload
	 */
	public Upload getUpload() {
		return upload;
	}

	/**
	 * @param template
	 *            the template to set
	 */
	public void setTemplate(final Template template) {
		this.template = template;
		backupTemplate = GsonHelper.fromJSON(GsonHelper.toJSON(template), Template.class);
	}

	/**
	 * @param upload
	 *            the upload to set
	 */
	public void setUpload(final Upload upload) {
		this.upload = upload;
		backupUpload = GsonHelper.fromJSON(GsonHelper.toJSON(upload), Upload.class);
	}

	public void reset() {
		template = GsonHelper.fromJSON(GsonHelper.toJSON(backupTemplate), Template.class);
		upload = GsonHelper.fromJSON(GsonHelper.toJSON(backupUpload), Upload.class);
	}

}