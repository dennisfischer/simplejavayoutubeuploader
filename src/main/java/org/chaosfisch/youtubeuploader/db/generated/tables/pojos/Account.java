/**
 * This class is generated by jOOQ
 */
package org.chaosfisch.youtubeuploader.db.generated.tables.pojos;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "3.0.0"},
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked" })
public class Account implements java.io.Serializable {

	private static final long serialVersionUID = -1011939423;

	private java.lang.Integer id;
	private java.lang.String  name;
	private java.lang.String  password;
	private java.lang.String  type;

	public java.lang.Integer getId() {
		return this.id;
	}

	public void setId(java.lang.Integer id) {
		this.id = id;
	}

	public java.lang.String getName() {
		return this.name;
	}

	public void setName(java.lang.String name) {
		this.name = name;
	}

	public java.lang.String getPassword() {
		return this.password;
	}

	public void setPassword(java.lang.String password) {
		this.password = password;
	}

	public java.lang.String getType() {
		return this.type;
	}

	public void setType(java.lang.String type) {
		this.type = type;
	}
}
