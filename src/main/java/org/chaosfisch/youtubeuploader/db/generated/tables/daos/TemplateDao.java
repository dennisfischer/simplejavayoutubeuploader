/**
 * This class is generated by jOOQ
 */
package org.chaosfisch.youtubeuploader.db.generated.tables.daos;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "3.0.0"},
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked" })
public class TemplateDao extends org.jooq.impl.DAOImpl<org.chaosfisch.youtubeuploader.db.generated.tables.records.TemplateRecord, org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template, java.lang.Integer> {

	/**
	 * Create a new TemplateDao without any factory
	 */
	public TemplateDao() {
		super(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE, org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template.class);
	}

	/**
	 * Create a new TemplateDao with an attached factory
	 */
	public TemplateDao(org.jooq.impl.Executor factory) {
		super(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE, org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template.class, factory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected java.lang.Integer getId(org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template object) {
		return object.getId();
	}

	/**
	 * Fetch records that have <code>ID IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchById(java.lang.Integer... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.ID, values);
	}

	/**
	 * Fetch a unique record that has <code>ID = value</code>
	 */
	public org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template fetchOneById(java.lang.Integer value) {
		return fetchOne(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.ID, value);
	}

	/**
	 * Fetch records that have <code>CATEGORY IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByCategory(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.CATEGORY, values);
	}

	/**
	 * Fetch records that have <code>COMMENT IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByComment(java.lang.Short... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.COMMENT, values);
	}

	/**
	 * Fetch records that have <code>COMMENTVOTE IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByCommentvote(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.COMMENTVOTE, values);
	}

	/**
	 * Fetch records that have <code>DEFAULTDIR IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByDefaultdir(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.DEFAULTDIR, values);
	}

	/**
	 * Fetch records that have <code>DESCRIPTION IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByDescription(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.DESCRIPTION, values);
	}

	/**
	 * Fetch records that have <code>EMBED IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByEmbed(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.EMBED, values);
	}

	/**
	 * Fetch records that have <code>KEYWORDS IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByKeywords(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.KEYWORDS, values);
	}

	/**
	 * Fetch records that have <code>MOBILE IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMobile(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MOBILE, values);
	}

	/**
	 * Fetch records that have <code>NAME IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByName(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.NAME, values);
	}

	/**
	 * Fetch records that have <code>NUMBER IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByNumber(java.lang.Short... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.NUMBER, values);
	}

	/**
	 * Fetch records that have <code>RATE IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByRate(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.RATE, values);
	}

	/**
	 * Fetch records that have <code>VIDEORESPONSE IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByVideoresponse(java.lang.Short... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.VIDEORESPONSE, values);
	}

	/**
	 * Fetch records that have <code>VISIBILITY IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByVisibility(java.lang.Short... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.VISIBILITY, values);
	}

	/**
	 * Fetch records that have <code>ACCOUNT_ID IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByAccountId(java.lang.Integer... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.ACCOUNT_ID, values);
	}

	/**
	 * Fetch records that have <code>ENDDIR IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByEnddir(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.ENDDIR, values);
	}

	/**
	 * Fetch records that have <code>LICENSE IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByLicense(java.lang.Short... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.LICENSE, values);
	}

	/**
	 * Fetch records that have <code>TITLE IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByTitle(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.TITLE, values);
	}

	/**
	 * Fetch records that have <code>THUMBNAIL IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByThumbnail(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.THUMBNAIL, values);
	}

	/**
	 * Fetch records that have <code>FACEBOOK IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByFacebook(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.FACEBOOK, values);
	}

	/**
	 * Fetch records that have <code>TWITTER IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByTwitter(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.TWITTER, values);
	}

	/**
	 * Fetch records that have <code>MESSAGE IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMessage(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MESSAGE, values);
	}

	/**
	 * Fetch records that have <code>INSTREAMDEFAULTS IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByInstreamdefaults(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.INSTREAMDEFAULTS, values);
	}

	/**
	 * Fetch records that have <code>CLAIM IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByClaim(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.CLAIM, values);
	}

	/**
	 * Fetch records that have <code>OVERLAY IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByOverlay(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.OVERLAY, values);
	}

	/**
	 * Fetch records that have <code>TRUEVIEW IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByTrueview(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.TRUEVIEW, values);
	}

	/**
	 * Fetch records that have <code>INSTREAM IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByInstream(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.INSTREAM, values);
	}

	/**
	 * Fetch records that have <code>PRODUCT IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByProduct(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.PRODUCT, values);
	}

	/**
	 * Fetch records that have <code>SYNDICATION IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchBySyndication(java.lang.Integer... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.SYNDICATION, values);
	}

	/**
	 * Fetch records that have <code>MONETIZETITLE IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizetitle(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZETITLE, values);
	}

	/**
	 * Fetch records that have <code>MONETIZEDESCRIPTION IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizedescription(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZEDESCRIPTION, values);
	}

	/**
	 * Fetch records that have <code>MONETIZEID IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizeid(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZEID, values);
	}

	/**
	 * Fetch records that have <code>MONETIZENOTES IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizenotes(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZENOTES, values);
	}

	/**
	 * Fetch records that have <code>MONETIZETMSID IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizetmsid(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZETMSID, values);
	}

	/**
	 * Fetch records that have <code>MONETIZEISAN IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizeisan(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZEISAN, values);
	}

	/**
	 * Fetch records that have <code>MONETIZEEIDR IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizeeidr(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZEEIDR, values);
	}

	/**
	 * Fetch records that have <code>MONETIZETITLEEPISODE IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizetitleepisode(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZETITLEEPISODE, values);
	}

	/**
	 * Fetch records that have <code>MONETIZESEASONNB IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizeseasonnb(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZESEASONNB, values);
	}

	/**
	 * Fetch records that have <code>MONETIZEEPISODENB IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizeepisodenb(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZEEPISODENB, values);
	}

	/**
	 * Fetch records that have <code>MONETIZECLAIMTYPE IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizeclaimtype(java.lang.Integer... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZECLAIMTYPE, values);
	}

	/**
	 * Fetch records that have <code>MONETIZECLAIMPOLICY IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizeclaimpolicy(java.lang.Integer... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZECLAIMPOLICY, values);
	}

	/**
	 * Fetch records that have <code>MONETIZEASSET IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizeasset(java.lang.Integer... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZEASSET, values);
	}

	/**
	 * Fetch records that have <code>MONETIZEPARTNER IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template> fetchByMonetizepartner(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Template.TEMPLATE.MONETIZEPARTNER, values);
	}
}
