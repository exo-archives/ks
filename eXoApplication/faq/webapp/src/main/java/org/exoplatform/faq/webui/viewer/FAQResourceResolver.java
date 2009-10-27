package org.exoplatform.faq.webui.viewer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.resolver.ResourceResolver;

public class FAQResourceResolver extends ResourceResolver{

	@Override
	public InputStream getInputStream(String url) throws Exception {
		ExoContainer container = ExoContainerContext.getCurrentContainer() ;
		FAQService faqService = (FAQService)container.getComponentInstanceOfType(FAQService.class) ;
		byte[] data = faqService.getTemplate() ;
		return new ByteArrayInputStream(data) ;		
	}

	@Override
	public List<InputStream> getInputStreams(String url) throws Exception {
		ArrayList<InputStream>  inputStreams = new ArrayList<InputStream>(1) ;
    inputStreams.add(getInputStream(url)) ;
    return inputStreams ;
	}

	@Override
	public URL getResource(String arg0) throws Exception {
		throw new Exception("This method is not  supported") ;  
	}

	@Override
	public String getResourceScheme() {
		return "jcr:" ;
	}

	@Override
	public List<URL> getResources(String arg0) throws Exception {
		throw new Exception("This method is not  supported") ;  
	}

	@Override
	public boolean isModified(String arg0, long arg1) {
		return false;
	}

}
