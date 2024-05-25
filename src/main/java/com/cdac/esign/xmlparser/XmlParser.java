package com.cdac.esign.xmlparser;

import java.io.File;
import java.io.FileOutputStream;
import java.security.PrivateKey;

import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.utils.EncryptionConstants;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class XmlParser {

  static{
  org.apache.xml.security.Init.init();
 }

 /**
  * Return DOM Document object for given xml file
  * 
  * @param xmlFile
  * @return
  * @throws Exception
  */
 public static Document getDocument(String xmlFile) throws Exception {
  /* Get the instance of BuilderFactory class. */
  DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();

  /* Instantiate DocumentBuilder object. */
  DocumentBuilder docBuilder = builder.newDocumentBuilder();

  /* Get the Document object */
  Document document = docBuilder.parse(xmlFile);
  return document;
 }

 /**
  * Save document to a file
  * 
  * @param document
  *            : Document to be saved
  * @param fileName
  *            : Represent file name to save
  * @throws Exception
  */
 public static void saveDocumentTo(Document document, String fileName)
   throws Exception {
  File encryptionFile = new File(fileName);
  FileOutputStream fOutStream = new FileOutputStream(encryptionFile);

  TransformerFactory factory = TransformerFactory.newInstance();
  Transformer transformer = factory.newTransformer();
  transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
  DOMSource source = new DOMSource(document);
  StreamResult result = new StreamResult(fOutStream);
  transformer.transform(source, result);

  fOutStream.close();
 }

 /**
  * Encrypt document with given algorithm and secret key.
  * 
  * @param document
  * @param secretKey
  * @param algorithm
  * @return
  * @throws Exception
  */
 public static Document encryptDocument(Document document,
   PrivateKey prevKey, String algorithm) throws Exception {
  /* Get Document root element */
  Element rootElement = document.getDocumentElement();
  String algorithmURI = algorithm;
  XMLCipher xmlCipher = XMLCipher.getInstance(algorithmURI);
  //XMLCipher xmlCipher = XMLCipher.getInstance();

  /* Initialize cipher with given secret key and operational mode */
  xmlCipher.init(XMLCipher.ENCRYPT_MODE, prevKey);

  /* Process the contents of document */
  xmlCipher.doFinal(document, rootElement, true);
  return document;
 }

 /**
  * Decrypt document using given key and algorithm
  * 
  * @param document
  * @param secretKey
  * @param algorithm
  * @return
  * @throws Exception
  */
 public static Document decryptDocument(Document document,
   SecretKey secretKey, String algorithm) throws Exception {
  Element encryptedDataElement = (Element) document
    .getElementsByTagNameNS(EncryptionConstants.EncryptionSpecNS,
      EncryptionConstants._TAG_ENCRYPTEDDATA).item(0);

  XMLCipher xmlCipher = XMLCipher.getInstance();

  xmlCipher.init(XMLCipher.DECRYPT_MODE, secretKey);
  xmlCipher.doFinal(document, encryptedDataElement);
  return document;
 }
}
