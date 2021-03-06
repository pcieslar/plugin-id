package org.ligoj.app.plugin.id.resource.batch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.app.DefaultVerificationMode;
import org.ligoj.app.iam.model.DelegateOrg;
import org.ligoj.app.model.ContainerType;
import org.ligoj.app.plugin.id.model.ContainerScope;
import org.ligoj.app.plugin.id.resource.ContainerScopeResource;
import org.ligoj.app.plugin.id.resource.GroupEditionVo;
import org.ligoj.app.plugin.id.resource.GroupResource;
import org.ligoj.bootstrap.core.SpringUtils;
import org.ligoj.bootstrap.resource.system.session.SessionSettings;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test of {@link GroupBatchResource}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
public class GroupBatchResourceTest extends AbstractBatchTest {

	@Autowired
	protected GroupBatchResource resource;

	private GroupResource mockResource;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void mockApplicationContext() {
		final ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
		SpringUtils.setSharedApplicationContext(applicationContext);
		mockResource = Mockito.mock(GroupResource.class);
		final GroupFullTask mockTask = new GroupFullTask();
		mockTask.resource = mockResource;
		mockTask.securityHelper = securityHelper;
		mockTask.containerScopeResource = Mockito.mock(ContainerScopeResource.class);
		Mockito.when(applicationContext.getBean(SessionSettings.class)).thenReturn(new SessionSettings());
		Mockito.when(applicationContext.getBean((Class<?>) ArgumentMatchers.any(Class.class))).thenAnswer((Answer<Object>) invocation -> {
			final Class<?> requiredType = (Class<Object>) invocation.getArguments()[0];
			if (requiredType == GroupFullTask.class) {
				return mockTask;
			}
			return GroupBatchResourceTest.super.applicationContext.getBean(requiredType);
		});

		final ContainerScope container = new ContainerScope();
		container.setId(1);
		container.setName("Fonction");
		container.setType(ContainerType.GROUP);
		Mockito.when(mockTask.containerScopeResource.findByName("Fonction")).thenReturn(container);
	}

	@AfterEach
	public void unmockApplicationContext() {
		SpringUtils.setSharedApplicationContext(super.applicationContext);
	}

	@BeforeEach
	public void prepareData() throws IOException {
		persistEntities("csv", new Class[] { DelegateOrg.class }, StandardCharsets.UTF_8.name());
	}

	@Test
	public void full() throws IOException, InterruptedException {
		final BatchTaskVo<GroupImportEntry> importTask = full("Gfi France;Fonction");

		// Check the result
		final GroupImportEntry importEntry = checkImportTask(importTask);
		Assertions.assertEquals("Gfi France", importEntry.getName());
		Assertions.assertEquals("Fonction", importEntry.getScope());
		Assertions.assertNull(importEntry.getDepartment());
		Assertions.assertNull(importEntry.getOwner());
		Assertions.assertNull(importEntry.getAssistant());
		Assertions.assertNull(importEntry.getParent());
		Assertions.assertTrue(importEntry.getStatus());
		Assertions.assertNull(importEntry.getStatusText());

		// Check group
		Mockito.verify(mockResource, new DefaultVerificationMode(data -> {
			if (data.getAllInvocations().size() != 1) {
				throw new MockitoException("Expect one call");
			}
			final GroupEditionVo group = (GroupEditionVo) data.getAllInvocations().get(0).getArguments()[0];
			Assertions.assertNotNull(group);
			Assertions.assertEquals("Gfi France", group.getName());
			Assertions.assertNotNull(group.getScope());
			Assertions.assertTrue(group.getDepartments().isEmpty());
			Assertions.assertTrue(group.getOwners().isEmpty());
			Assertions.assertTrue(group.getAssistants().isEmpty());
			Assertions.assertNull(group.getParent());
		})).create(null);
	}

	// "name", "type", "parent", "owner", "seealso", "department"
	@Test
	public void fullFull() throws IOException, InterruptedException {
		final BatchTaskVo<GroupImportEntry> importTask = full("Special;Fonction;Operations;700301,700302;fdaugan,alongchu;jdoe5,wuser");

		// Check the result
		final GroupImportEntry importEntry = checkImportTask(importTask);
		Assertions.assertEquals("Special", importEntry.getName());
		Assertions.assertEquals("Fonction", importEntry.getScope());
		Assertions.assertEquals("Operations", importEntry.getParent());
		Assertions.assertEquals("fdaugan,alongchu", importEntry.getOwner());
		Assertions.assertEquals("jdoe5,wuser", importEntry.getAssistant());
		Assertions.assertEquals("700301,700302", importEntry.getDepartment());
		Assertions.assertTrue(importEntry.getStatus());
		Assertions.assertNull(importEntry.getStatusText());

		// Check group
		Mockito.verify(mockResource, new DefaultVerificationMode(data -> {
			if (data.getAllInvocations().size() != 1) {
				throw new MockitoException("Expect one call");
			}
			final GroupEditionVo group = (GroupEditionVo) data.getAllInvocations().get(0).getArguments()[0];
			Assertions.assertNotNull(group);
			Assertions.assertEquals("Special", group.getName());
			Assertions.assertNotNull(group.getScope());
			Assertions.assertEquals(2, group.getOwners().size());
			Assertions.assertEquals("fdaugan", group.getOwners().get(0));
			Assertions.assertEquals(2, group.getAssistants().size());
			Assertions.assertEquals("jdoe5", group.getAssistants().get(0));
			Assertions.assertEquals(2, group.getDepartments().size());
			Assertions.assertEquals("700301", group.getDepartments().get(0));
			Assertions.assertEquals("Operations", group.getParent());
		})).create(null);
	}

	protected <U extends BatchElement> BatchTaskVo<U> full(final InputStream input, final String[] headers) throws IOException, InterruptedException {
		return full(input, headers, "cp1252");
	}

	protected <U extends BatchElement> BatchTaskVo<U> full(final InputStream input, final String[] headers, final String encoding)
			throws IOException, InterruptedException {
		initSpringSecurityContext(DEFAULT_USER);
		final long id = resource.full(input, headers, encoding, false);
		Assertions.assertNotNull(id);
		@SuppressWarnings("unchecked")
		final BatchTaskVo<U> importTask = (BatchTaskVo<U>) resource.getImportTask(id);
		Assertions.assertEquals(id, importTask.getId());
		return waitImport(importTask);
	}

	protected <U extends BatchElement> BatchTaskVo<U> full(final String csvData) throws IOException, InterruptedException {
		return full(csvData, "cp1252");
	}

	protected <U extends BatchElement> BatchTaskVo<U> full(final String csvData, final String encoding) throws IOException, InterruptedException {
		return full(new ByteArrayInputStream(csvData.getBytes(encoding)),
				new String[] { "name", "scope", "parent", "department", "owner", "assistant" }, encoding);
	}
}
