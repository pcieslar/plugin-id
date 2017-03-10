package org.ligoj.app.resource.plugin.id;

import org.springframework.stereotype.Component;

import org.ligoj.app.resource.plugin.AbstractServicePlugin;

/**
 * The bug tracker service.
 */
@Component
public class IdentityResource extends AbstractServicePlugin {

	/**
	 * Plug-in key.
	 */
	public static final String SERVICE_URL = BASE_URL + "/id";

	/**
	 * Plug-in key.
	 */
	public static final String SERVICE_KEY = SERVICE_URL.replace('/', ':').substring(1);

	/**
	 * Normalized LDAP Group name (CN).
	 */
	public static final String PARAMETER_GROUP = SERVICE_KEY + ":group";

	/**
	 * Normalized LDAP parent Group name (CN).
	 */
	public static final String PARAMETER_PARENT_GROUP = SERVICE_KEY + ":parent-group";

	/**
	 * Normalized LDAP Organizational Unit (OU).
	 */
	public static final String PARAMETER_OU = SERVICE_KEY + ":ou";

	/**
	 * Pattern determining the login is valid for a authentication.
	 */
	public static final String PARAMETER_UID_PATTERN = SERVICE_KEY + ":uid-pattern";

	@Override
	public String getKey() {
		return SERVICE_KEY;
	}
}