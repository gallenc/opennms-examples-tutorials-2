package org.opennms.examples.integration.exx1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadProperties {
	private static final Logger LOG = LoggerFactory.getLogger(LoadProperties.class);

	private static Properties properties = null;

	public static Properties load() {
		if (properties == null)
			synchronized (LoadProperties.class) {
				InputStream stream = null;
				if (properties == null) {
					try {
						String propertiesFile = System.getProperty("kafkaclient.properties");
						if (propertiesFile == null) {
							LOG.info("using default properties");
							stream = LoadProperties.class.getResourceAsStream("/kafkaclient.properties");
						} else {
							LOG.info("loading properties from: " + propertiesFile);
							File f = new File(propertiesFile);
							LOG.info("properties file: " + f.getAbsolutePath());
							stream = new FileInputStream(f);
						}

						Properties propertiesx = new Properties();
						propertiesx.load(stream);
						properties = propertiesx;
						LOG.info("loaded properties "+properties.toString());
					} catch (Exception ex) {
						LOG.error("cannot load properties: ", ex);
					} finally {
						if(stream!=null)
							try {
								stream.close();
							} catch (IOException e) {}
					}
				}
			}
		return properties;

	}

}
