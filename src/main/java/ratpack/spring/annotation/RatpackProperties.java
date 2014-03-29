/*
 * Copyright 2012-2013 the original author or authors.
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

package ratpack.spring.annotation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author Dave Syer
 * 
 */
@ConfigurationProperties(prefix = "server", ignoreUnknownFields = false)
public class RatpackProperties {

	private Integer port;

	private InetAddress address;

	private Integer sessionTimeout;

	private Resource basedir = new FileSystemResource(".");

	@NotNull
	private String contextPath = "";

	private int maxThreads = 0; // Number of threads in protocol handler

	private String templatesPath = "templates";
	private int cacheSize = 100;
	private boolean reloadable;
	private boolean staticallyCompile;

	public String getTemplatesPath() {
		return templatesPath;
	}

	public void setTemplatesPath(String templatesPath) {
		this.templatesPath = templatesPath;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public boolean isReloadable() {
		return reloadable;
	}

	public void setReloadable(boolean reloadable) {
		this.reloadable = reloadable;
	}

	public boolean isStaticallyCompile() {
		return staticallyCompile;
	}

	public void setStaticallyCompile(boolean staticallyCompile) {
		this.staticallyCompile = staticallyCompile;
	}

	public int getMaxThreads() {
		return this.maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public Path getBasepath() {
		try {
			return resourceToPath(this.basedir.getURL());
		} catch (IOException e) {
			throw new IllegalStateException("Cannot extract base dir URL", e);
		}
	}

	public Resource getBasedir() {
		return this.basedir;
	}

	public void setBasedir(Resource basedir) {
		this.basedir = basedir;
	}

	public String getContextPath() {
		return this.contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public InetAddress getAddress() {
		return this.address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public Integer getSessionTimeout() {
		return this.sessionTimeout;
	}

	public void setSessionTimeout(Integer sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	static Path resourceToPath(URL resource) {

		Objects.requireNonNull(resource, "Resource URL cannot be null");
		URI uri;
		try {
			uri = resource.toURI();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Could not extract URI", e);
		}

		String scheme = uri.getScheme();
		if (scheme.equals("file")) {
			return Paths.get(uri);
		}

		if (!scheme.equals("jar")) {
			throw new IllegalArgumentException("Cannot convert to Path: " + uri);
		}

		String s = uri.toString();
		int separator = s.indexOf("!/");
		String entryName = s.substring(separator + 2);
		URI fileURI = URI.create(s.substring(0, separator));

		FileSystem fs;
		try {
			fs = FileSystems.newFileSystem(fileURI,
					Collections.<String, Object> emptyMap());
			return fs.getPath(entryName);
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Could not create file system for resource: " + resource, e);
		}
	}

}
