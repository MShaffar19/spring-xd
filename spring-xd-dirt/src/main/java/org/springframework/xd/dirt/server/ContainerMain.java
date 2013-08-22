/*
 * Copyright 2013 the original author or authors.
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

package org.springframework.xd.dirt.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.xd.dirt.container.XDContainer;
import org.springframework.xd.dirt.launcher.ContainerLauncher;
import org.springframework.xd.dirt.server.options.ContainerOptions;
import org.springframework.xd.dirt.server.options.OptionUtils;

/**
 * The main driver class for the container
 * 
 * @author Mark Pollack
 * @author Jennifer Hickey
 * @author Ilayaperumal Gopinathan
 * @author Mark Fisher
 * @author David Turanski
 */
public class ContainerMain {

	private static final Log logger = LogFactory.getLog(ContainerMain.class);

	private static final String LAUNCHER_CONFIG_LOCATION = XDContainer.XD_INTERNAL_CONFIG_ROOT + "launcher.xml";

	/**
	 * Start the RedisContainerLauncher
	 * 
	 * @param args command line argument
	 */
	public static void main(String[] args) {
		ContainerOptions options = new ContainerOptions();
		CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
		}
		catch (CmdLineException e) {
			logger.error(e.getMessage());
			parser.printUsage(System.err);
			System.exit(1);
		}

		if (options.isShowHelp()) {
			parser.printUsage(System.err);
			System.exit(0);
		}
		launch(options, null);
	}

	/**
	 * Create a container instance
	 * 
	 * @param options
	 */
	@SuppressWarnings("resource")
	public static XDContainer launch(ContainerOptions options, ApplicationContext parentContext) {
		ClassPathXmlApplicationContext context = null;

		context = new ClassPathXmlApplicationContext();
		context.setConfigLocation(LAUNCHER_CONFIG_LOCATION);

		OptionUtils.configureRuntime(options, context.getEnvironment());

		if (parentContext == null) {
			parentContext = createParentContext();
		}

		context.setParent(parentContext);
		context.refresh();
		context.registerShutdownHook();

		ContainerLauncher launcher = context.getBean(ContainerLauncher.class);
		XDContainer container = launcher.launch(options);
		return container;
	}

	private static ApplicationContext createParentContext() {
		XmlWebApplicationContext parentContext = new XmlWebApplicationContext();
		parentContext.setConfigLocation("classpath:" + XDContainer.XD_INTERNAL_CONFIG_ROOT + "xd-global-beans.xml");
		parentContext.refresh();
		return parentContext;
	}

}
