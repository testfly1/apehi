package com.axa.api.model.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Repository;

import com.axa.api.model.response.api.TokenObject;

/**
 * Model : Data Access Object for exchanges with LDAP
 * 
 */
@Repository
public class TokenDAO {

	@Autowired
	private LdapTemplate ldapTemplate;

	/**
	 * Creation of a token in LDAP
	 * @param TokenObject tokenObject
	 */
	public void create(TokenObject tokenObject) {
		ldapTemplate.create(tokenObject);
	}
	
	/**
	 * Deletion of a token in LDAP
	 * @param TokenObject tokenObject
	 */
	public void delete(TokenObject tokenObject) {
		ldapTemplate.delete(tokenObject);
	}
	
	/**
	 * Update status of an object in LDAP
	 * @param TokenObject tokenObject
	 */
	public void update(TokenObject tokenObject) {
		ldapTemplate.update(tokenObject);
	}
	
	/**
	 * Recovery of a token in LDAP
	 * @param String userIdentifier
	 * @param String channel
	 * @param String schema
	 * @return tokenObject
	 */
	public TokenObject get(String userIdentifier, String channel, String schema) {
		LdapQuery lq = LdapQueryBuilder.query()
				.where("userIdentifier").is(userIdentifier)
				.and("channel").is(channel)
				.and("schema").is(schema);

		return ldapTemplate.findOne(lq, TokenObject.class);
	}
}
