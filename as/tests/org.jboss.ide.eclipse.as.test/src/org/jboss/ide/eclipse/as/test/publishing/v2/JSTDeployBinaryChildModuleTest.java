package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.test.publishing.AbstractDeploymentTest;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;

public class JSTDeployBinaryChildModuleTest extends JSTDeploymentTester {
	protected IProject createProject() throws Exception {
		IDataModel dm = ProjectCreationUtil.getWebDataModel(MODULE_NAME, null, null, null, null, JavaEEFacetConstants.WEB_24, false);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(MODULE_NAME);
		assertTrue(p.exists());
		File srcFile = AbstractDeploymentTest.getFileLocation("projectPieces/EJB3NoDescriptor.jar");
		String proj = p.getLocation().toOSString();
		p.getFolder("WebContent").getFolder("WEB-INF")
			.getFolder("lib").getFile("test.jar").create(
				new FileInputStream(srcFile), true, new NullProgressMonitor());
		p.refreshLocal(0, new NullProgressMonitor());
		return p;
	}

	public void testMain() throws CoreException, IOException {
		IModule mod = ServerUtil.getModule(project);
		IModule[] module = new IModule[] { mod };
		verifyJSTPublisher(module);
		addModule(mod);
		publish();
		IPath deployRoot = new Path(getDeployRoot(server));
		IPath rootFolder = deployRoot.append(MODULE_NAME + ".war");
		assertTrue(rootFolder.toFile().exists());
		IPath webinf_lib_testjar = rootFolder.append("WEB-INF").append("lib").append("test.jar");
		assertTrue("test.jar exists in deployment", webinf_lib_testjar.toFile().exists());
		assertTrue("test.jar File is actually a file", webinf_lib_testjar.toFile().isFile());
	}
}