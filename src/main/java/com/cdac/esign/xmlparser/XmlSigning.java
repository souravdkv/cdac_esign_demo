package com.cdac.esign.xmlparser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Collections;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.ElementProxy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.apache.xml.security.utils.Constants;
//import org.apache.xml.security.signature.SignedInfo;
//import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.cdac.esign.encryptor.Encryption;

/**
 * This class is used to provide convenient methods to digitally sign an XML
 * document.
 *
 *
 */
public class XmlSigning {

    /**
     * Method used to get the XML document by parsing
     *
     * @param xmlFilePath , file path of the XML document
     * @return Document
     */
	//Encryption encryption = new Encryption();
    public Document getXmlDocument(String xmlFilePath) {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            doc = dbf.newDocumentBuilder().parse(new FileInputStream(xmlFilePath));
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return doc;
    }

    /**
     * Method used to get the KeyInfo
     *
     * @param xmlSigFactory
     * @param publicKeyPath
     * @return KeyInfo
     */
    public KeyInfo getKeyInfo(XMLSignatureFactory xmlSigFactory, String publicKeyPath) {
        KeyInfo keyInfo = null;
        KeyValue keyValue = null;
        
        KeyInfoFactory keyInfoFact = xmlSigFactory.getKeyInfoFactory();

        try {
        	PublicKey privKey = new Encryption().getPublicKey("testasp.cer");
            keyValue = keyInfoFact.newKeyValue(privKey);
        }catch (KeyException ex) {
            ex.printStackTrace();
        }catch(Exception e) {
            e.printStackTrace();
            }
        keyInfo = keyInfoFact.newKeyInfo(Collections.singletonList(keyValue));
        return keyInfo;
    }

    /*
     * Method used to store the signed XMl document
     */
    public void storeSignedDoc(Document doc, String destnSignedXmlFilePath) {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer trans = null;
        try {
            trans = transFactory.newTransformer();
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
        }
        try {
            StreamResult streamRes = new StreamResult(new File(destnSignedXmlFilePath));
            trans.transform(new DOMSource(doc), streamRes);
        } catch (TransformerException ex) {
            ex.printStackTrace();
        }
        System.out.println("XML file with attached digital signature generated successfully ...");
    }

   
    
    public String signXmlStringNew(String xmlIn, PrivateKey privateKey)throws Exception {
    	// Create a DOM XMLSignatureFactory that will be used to
    	// generate the enveloped signature.
    	XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

    	// Create a Reference to the enveloped document (in this case,
    	// you are signing the whole document, so a URI of "" signifies
    	// that, and also specify the SHA1 digest algorithm and
    	// the ENVELOPED Transform.
    	Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256,
    	null),
    	Collections.singletonList(fac.newTransform(Transform.ENVELOPED,
    	(TransformParameterSpec) null)),
    	null, null);

    	// Create the SignedInfo.
    	SignedInfo si =
    	fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
    	(C14NMethodParameterSpec) null),
    	fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
    	Collections.singletonList(ref));

    	// read public key DER file
    	// read private key DER file
    	// Load the KeyStore and get the signing key and certificate.
    	//RSAPrivateKey privKey = getPrivateKey();

    	// Instantiate the document to be signed.
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	dbf.setNamespaceAware(true);
    	Document doc = getXmlDocument(xmlIn);
    	
    	// Create a DOMSignContext and specify the RSA PrivateKey and
    	// location of the resulting XMLSignature's parent element.
    	DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());

    	// Create the XMLSignature, but don't sign it yet.
    	XMLSignature signature = fac.newXMLSignature(si, null);

    	// Marshal, generate, and sign the enveloped signature.
    	signature.sign(dsc);

    	// Output the resulting document.
    	OutputStream os = new ByteArrayOutputStream();
    	TransformerFactory tf = TransformerFactory.newInstance();
    	Transformer trans = tf.newTransformer();

    	trans.transform(new DOMSource(doc), new StreamResult(os));

    	return os.toString();
      }
    
    public String parseXml(String esignResponse)throws Exception {
    
    	DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(esignResponse));

        Document doc = db.parse(is);
    	//Document doc = getXmlDocument(esignResponse);
    	NodeList node = doc.getElementsByTagName("DocSignature");
    	String sig = node.item(0).getTextContent();
    	return sig;  
    }
    
    
}
