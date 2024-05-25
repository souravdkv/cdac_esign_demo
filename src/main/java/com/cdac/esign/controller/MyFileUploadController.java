package com.cdac.esign.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.text.DateFormat;
//import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.xml.security.encryption.XMLCipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.apache.xml.security.encryption.XMLCipher;
import org.w3c.dom.Document;

import com.cdac.esign.form.MyUploadForm;
import com.cdac.esign.form.RequestXmlForm;
import com.cdac.esign.form.FormXmlDataAsp;
import com.cdac.esign.xmlparser.AspXmlGenerator;
import com.cdac.esign.xmlparser.XmlParser;
import com.cdac.esign.xmlparser.XmlSigning;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfString;
import com.cdac.esign.encryptor.Encryption;
import com.cdac.esign.validator.FieldValidator;

 
@Controller
//@Scope("request")
//("session")s
public class MyFileUploadController {
	
	@Autowired
	private PdfEmbedder pdfEmbedder;
	
	@Autowired
	private HttpSession session;
 
  /* @RequestMapping(value = "/")
   public String homePage() {
 
      return "index";
   }*/
 
   // GET: Show upload form page.
   //@RequestMapping(value = "/uploadOneFile", method = RequestMethod.GET)
   @RequestMapping(value = "/")
   public String uploadOneFileHandler(Model model) {
 
      MyUploadForm myUploadForm = new MyUploadForm();
      model.addAttribute("myUploadForm", myUploadForm);
      System.out.println("**************************************"+session.getId());
      return "uploadOneFile";
   }
 

   @RequestMapping(value = "/uploadMultiFile", method = RequestMethod.POST)
   @ResponseBody
   public RequestXmlForm saveEmployee(@ModelAttribute @Valid MyUploadForm myUploadForm,
         BindingResult result, HttpServletRequest request, Model model, HttpSession session) {
	   System.out.println("**************************************"+session.getId());
	   return this.doProcess(request, model, myUploadForm, session);
   }
   
   private RequestXmlForm doProcess(HttpServletRequest request, Model model, //
	         MyUploadForm myUploadForm, HttpSession session) {
	   System.out.println("**************************************"+session.getId());
		   FieldValidator fieldValidator = new FieldValidator();
		  // PdfEmbedder pdfEmbedder = new PdfEmbedder();
		   if(fieldValidator.validateFields(myUploadForm.getAadhar(), "Y", myUploadForm.getAuthType(), myUploadForm.getFileDatas())) {
		   
			  // Root Directory.
		      String uploadRootPath = request.getServletContext().getRealPath("upload");
		      System.out.println("uploadRootPath=" + uploadRootPath);
		 
		      File uploadRootDir = new File(uploadRootPath);
		      // Create directory if it not exists.
		      if (!uploadRootDir.exists()) {
		         uploadRootDir.mkdirs();
		      }
		      MultipartFile[] fileDatas = myUploadForm.getFileDatas();
		      //
		      List<File> uploadedFiles = new ArrayList<File>();
		      List<String> failedFiles = new ArrayList<String>();
		
		 	  String fileHash = "";
		      for (MultipartFile fileData : fileDatas) {
		 
		         // Client File Name
		         String name = fileData.getOriginalFilename();
		         System.out.println("Client File Name = " + name);
		 
		         if (name != null && name.length() > 0) {
		            try {
		               // Create the file at server
		               File serverFile = new File(uploadRootDir.getAbsolutePath() + File.separator + name);
		 
		               BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
		               stream.write(fileData.getBytes());
		               stream.close();
		               //
		               uploadedFiles.add(serverFile);
		               //fileHash = calculateFileHash(uploadRootDir.getAbsolutePath() + File.separator + name);
		               fileHash = pdfEmbedder.pdfSigner(serverFile,request, session);
		               request.getSession().setAttribute("pdfEmbedder", pdfEmbedder);
		               System.out.println("Write file: " + serverFile);
		            } catch (Exception e) {
		               System.out.println("Error Write file: " + name);
		               failedFiles.add(name);
		            }
		         }
		      }
			   
			  		  
			  //get data from Form
		      String authType = myUploadForm.getAuthType();
		      System.out.println("Description: " + authType);
		      System.out.println("Aadhar: " + myUploadForm.getAadhar());
		      System.out.println("Consent: " + myUploadForm.getConsent());
		 	            
		      Date now = new Date();
		      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
			   //try xml generation
			  AspXmlGenerator aspXmlGenerator = new AspXmlGenerator();
			  FormXmlDataAsp formXmalDataAsp = new FormXmlDataAsp();
			  Random randNum = new Random();
			  int randInt = randNum.nextInt();
			  formXmalDataAsp.setVer("2.1");
			  formXmalDataAsp.setSc("Y");
			  formXmalDataAsp.setTs(dateFormat.format(now));
			  //formXmalDataAsp.setTxn((myUploadForm.getAadhar() + randInt).replace("-", ""));
			  formXmalDataAsp.setTxn(("" + randInt).replace("-", ""));
			 formXmalDataAsp.setEkycId("");
			 //formXmalDataAsp.setEkycId("1916c708a819a70b098b71f8d95f9936e9b86a90d948107e4b9172d65bceefa5");
			  formXmalDataAsp.setEkycIdType("A");
			 formXmalDataAsp.setAspId("CDAC-901");
			  formXmalDataAsp.setAuthMode(myUploadForm.getAuthType());
			  formXmalDataAsp.setResponseSigType("pkcs7");
			  //formXmalDataAsp.setResponseUrl("url");
			  formXmalDataAsp.setResponseUrl("https://10.208.37.160:9090/SpringBootESign/finalResponse");
			  
			  formXmalDataAsp.setId("1");
			  formXmalDataAsp.setHashAlgorithm("SHA256");
			  formXmalDataAsp.setDocInfo("My Document");
			  formXmalDataAsp.setDocHashHex(fileHash);

			  //Get encrypted string/ signed data for xml signature tag
		      String strToEncrypt = aspXmlGenerator.generateAspXml(formXmalDataAsp,request);
		      String encryptedText = "";
		      String xmlData = "";
			  try {
		      Encryption encryption = new Encryption();
			  PrivateKey rsaPrivateKey =  encryption.getPrivateKey("testasp.pem");
			  File encrFile = new File(uploadRootDir.getAbsolutePath() + File.separator + "Excrypted.xml");
			  String encryptedFile = uploadRootDir.getAbsolutePath() + File.separator + "Excrypted.xml";
			  xmlData = new XmlSigning().signXmlStringNew(uploadRootDir.getAbsolutePath() + File.separator + "Testing.xml", rsaPrivateKey);
			  System.out.println(xmlData);
			  aspXmlGenerator.writeToXmlFile(xmlData,uploadRootDir.getAbsolutePath() + File.separator + "Testing.xml");
			  
			  
		      }
		      catch(Exception e) {
		    	  System.out.println("Error in Encryption.");
		    	  e.printStackTrace();
		    	  return new RequestXmlForm();
		      }
			  
			  RequestXmlForm myRequestXmlForm = new RequestXmlForm();
			  myRequestXmlForm.setId("");
			  myRequestXmlForm.setType(myUploadForm.getAuthType());
			  myRequestXmlForm.setDescription("Y");
			  myRequestXmlForm.seteSignRequest(xmlData);
			  myRequestXmlForm.setAspTxnID(("" + randInt).replaceAll("-",""));
			  myRequestXmlForm.setContentType("application/xml");
			  myUploadForm.setXml(xmlData);
		      return myRequestXmlForm;
		   }
		   else {
			   return new RequestXmlForm();
		   }
	   }   
   
 

   @RequestMapping(value = "/embedDigSign", method = RequestMethod.GET)
   public String embedDigSign(HttpServletResponse response) { 	   
	      return "success"; 
   }
   
   @RequestMapping(value = "/finalResponse", method = RequestMethod.POST )
   public String ReadEspResponse(@RequestParam("eSignResponse") String response,@RequestParam("espTxnID") String espId,RedirectAttributes rdAttr, HttpServletRequest request) throws IOException {
	  // HttpSession session = request.getSession(false);
   //PdfEmbedder pdfEmbedder = (PdfEmbedder)request.getSession().getAttribute("pdfEmbedder");
    System.out.println("**************************************"+session.getId());
    System.out.println("**************************************"+response);
   String filename = pdfEmbedder.signPdfwithDS(response, request, session);
   System.out.println(" Response--->"+response+"ESP ID"+espId);
   if(filename.equals("Error")) {
	   String error = response.substring(response.indexOf("errCode"),response.indexOf("resCode"));
	   ModelAndView model = new ModelAndView();
	   model.addObject("error", error);
	      System.out.println("**************************************"+session.getId());
	      return "errorFile";
   }else {
	   return "downloadPdf";
   }
   }

   /*@RequestMapping("/downloadPdfLocally")
	public void downloadPDFResource(HttpServletRequest request, HttpServletResponse response) throws IOException {
	String uploadRootPath = request.getServletContext().getRealPath("upload");
	   File file = new File(uploadRootPath + "//" + "Signed_Pdf.pdf");
		if (file.exists()) {

			//get the mimetype
			String mimeType = URLConnection.guessContentTypeFromName(file.getName());
			if (mimeType == null) {
				//unknown mimetype so set the mimetype to application/octet-stream
				mimeType = "application/octet-stream";
			}

			response.setContentType(mimeType);
			response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

			 //Here we have mentioned it to show as attachment
			response.setContentLength((int) file.length());

			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

			FileCopyUtils.copy(inputStream, response.getOutputStream());

		}
	}*/
   
   @RequestMapping("/downloadPdfLocally")
	public void downloadPDFResource(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//If user is not authorized - he should be thrown out from here itself
			
			//Authorized user will download the file
			String dataDirectory = request.getServletContext().getRealPath("upload");
			Path file = Paths.get(dataDirectory, "Signed_Pdf.pdf");
			if (Files.exists(file))
				{
					response.setContentType("application/pdf");
					response.addHeader("Content-Disposition", "attachment; filename="+"Signed_Pdf.pdf");
				try
				{
					Files.copy(file, response.getOutputStream());
					response.getOutputStream().flush();
				}
					catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
}