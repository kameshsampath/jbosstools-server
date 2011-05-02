/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.launch.AbstractJBossLaunchConfigType;

public class JBossRuntimeClasspathUtil {

	private static final String JBOSS_MODULES_JAR = "jboss-modules.jar"; //$NON-NLS-1$

	public static List<String> getClasspath(IServer server, IVMInstall vmInstall) throws CoreException {
		List<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
		classpath.add(getModulesClasspathEntry(server));
		AbstractJBossLaunchConfigType.addJREEntry(classpath, vmInstall);
		List<String> runtimeClassPaths = AbstractJBossLaunchConfigType.convertClasspath(classpath);
		return runtimeClassPaths;
	}

	public static IRuntimeClasspathEntry getModulesClasspathEntry(IServer server) throws CoreException {
		IPath runtimeLocation = server.getRuntime().getLocation();
		IPath modulesLocation = runtimeLocation.append(JBOSS_MODULES_JAR);
		IClasspathEntry entry =
				JavaRuntime.newArchiveRuntimeClasspathEntry(modulesLocation).getClasspathEntry();
		return new RuntimeClasspathEntry(entry);
	}

}
