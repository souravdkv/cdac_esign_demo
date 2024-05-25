package com.cdac.esign.form;

import org.springframework.web.multipart.MultipartFile;

public class MyUploadForm {
 
    private String authType;
    private String consent;
    private String aadhar;
    private String xml;
    
	// Upload files.
    private MultipartFile[] fileDatas;
 
    public String getConsent() {
		return consent;
	}

	public void setConsent(String consent) {
		this.consent = consent;
	}

	public String getAadhar() {
		return aadhar;
	}

	public void setAadhar(String aadhar) {
		this.aadhar = aadhar;
	}

	public String getAuthType() {
        return authType;
    }
 
    public void setAuthType(String authType) {
        this.authType = authType;
    }
 
    public MultipartFile[] getFileDatas() {
        return fileDatas;
    }
 
    public void setFileDatas(MultipartFile[] fileDatas) {
        this.fileDatas = fileDatas;
    }
 
    public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}
 
}