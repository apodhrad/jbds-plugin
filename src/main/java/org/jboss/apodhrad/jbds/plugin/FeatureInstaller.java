package org.jboss.apodhrad.jbds.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Set;

import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jboss.apodhrad.jbds.plugin.eclipse.Eclipse;

/**
 * @author apodhrad
 * 
 *         Installs Eclipse feature
 */
@Mojo(name = "install-feature", defaultPhase = LifecyclePhase.COMPILE)
public class FeatureInstaller extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.directory}")
	private String target;

	@SuppressWarnings("rawtypes")
	@Parameter(defaultValue = "${project.remoteArtifactRepositories}")
	private java.util.List remoteRepositories;

	@Parameter(alias = "eclipse.home")
	private String eclipseHome;

	@Parameter
	private Set<String> features;

	public void execute() throws MojoExecutionException {
		getLog().info("ECLIPSE_HOME=" + eclipseHome);
		Eclipse eclipse = new Eclipse(eclipseHome);

		for (Object obj : remoteRepositories) {
			if (obj instanceof MavenArtifactRepository) {
				MavenArtifactRepository repo = (MavenArtifactRepository) obj;
				if (repo.getLayout().getId().equals("update-site")) {
					getLog().info("Added update site " + repo.getId() + " at " + repo.getUrl());
					eclipse.addUpdateSite(repo.getUrl());
				}
			}
		}
		
		eclipse.installFeatures(features);

		getLog().info("Done.");
	}

}
