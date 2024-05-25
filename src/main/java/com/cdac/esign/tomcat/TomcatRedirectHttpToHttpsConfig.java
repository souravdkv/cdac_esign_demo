//package com.cdac.esign.tomcat;
//
//import org.apache.catalina.Context;
//import org.apache.catalina.connector.Connector;
//import org.apache.coyote.http11.AbstractHttp11Protocol;
//import org.apache.tomcat.util.descriptor.web.SecurityCollection;
//import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
//import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
//import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class TomcatRedirectHttpToHttpsConfig {
//	@Bean
//	public TomcatServletWebServerFactory containerFactory() {
//        return new TomcatServletWebServerFactory() {
//            protected void customizeConnector(Connector connector) {
//                int maxSize = 50000000;
//                super.customizeConnector(connector);
//                connector.setMaxPostSize(maxSize);
//                connector.setMaxSavePostSize(maxSize);
//                if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {
//
//                    ((AbstractHttp11Protocol <?>) connector.getProtocolHandler()).setMaxSwallowSize(maxSize);
//                    logger.info("Set MaxSwallowSize "+ maxSize);
//                }
//            }
//        };
//
//    }
//	
//	TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
//	    @Override
//	    protected void postProcessContext(Context context) {
//	        SecurityConstraint securityConstraint = new SecurityConstraint();
//	        securityConstraint.setUserConstraint("CONFIDENTIAL");
//	        SecurityCollection collection = new SecurityCollection();
//	        collection.addPattern("/*");
//	        securityConstraint.addCollection(collection);
//	        context.addConstraint(securityConstraint);
//	    }
//	};

//    private Connector redirectConnector(){
//        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
//        connector.setScheme("http");
//        connector.setPort(8080);
//        connector.setSecure(false);
//        connector.setRedirectPort(8443);
//        return connector;
//    }
    
//    @Bean
//    public TomcatServletWebServerFactory servletContainer(){
//    	TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
//        tomcat.addAdditionalTomcatConnectors(httpConnector());
//        return tomcat;
//    }

//    private Connector httpConnector(){
//        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
//        connector.setScheme("http");
//        connector.setPort(8080);
//        connector.setSecure(false);
//        connector.setRedirectPort(8443);
//        return connector;
//    }
//}
