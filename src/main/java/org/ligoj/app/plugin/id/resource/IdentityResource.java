package org.ligoj.app.plugin.id.resource;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import org.ligoj.app.model.Node;
import org.ligoj.app.model.Parameter;
import org.ligoj.app.resource.plugin.AbstractServicePlugin;

/**
 * The identity service.
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
	 * Normalized Group name (CN).
	 */
	public static final String PARAMETER_GROUP = SERVICE_KEY + ":group";

	/**
	 * Normalized parent Group name (CN).
	 */
	public static final String PARAMETER_PARENT_GROUP = SERVICE_KEY + ":parent-group";

	/**
	 * Normalized Organizational Unit (OU).
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

	@Override
	public List<Class<?>> getInstalledEntities() {
		return Arrays.asList(Node.class, Parameter.class);
	}
}
