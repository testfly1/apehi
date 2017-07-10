package com.axa.api.service;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.axa.api.configuration.yml.SchemaListConfig;
import com.axa.api.configuration.yml.SoapConfig;
import com.axa.api.exception.InvalidSchemaException;
import com.axa.api.model.dao.SoapDAO;
import com.axa.api.model.enumeration.ChannelEnum;
import com.axa.api.model.input.TokenInput;
import com.unboundid.util.Base64;

/**
 * Service : Actions about the SOAP message (generation of message and sending to server)
 *
 */
@Service
public class SoapService {

	@Autowired
	private SoapConfig soapConfig;
	
	@Autowired
	private SchemaListConfig schemaListConfig;
	
	@Autowired
	SoapDAO soapDAO;
	
	/**
	 * Generate SOAP request
	 * @param tokenInput
	 * @param code
	 * @throws SOAPException
	 * @throws InvalidSchemaException
	 */
	public void generateSOAPRequest(TokenInput tokenInput, String code) throws SOAPException, InvalidSchemaException{
		
		SOAPMessage request = createRequest(tokenInput, code);
		String url = soapConfig.getSoapServerUrl();
		
		soapDAO.sendSoapRequest(request, url);
	}
	
	/**
	 * Creation of the SOAP request according to the TokenInput, code and the configurations written in the .yml
	 * @param TokenInput tokenInput
	 * @param String code
	 * @return soapMessage
	 * @throws SOAPException
	 * @throws InvalidSchemaException
	 */
	private SOAPMessage createRequest(TokenInput tokenInput, String code) throws SOAPException, InvalidSchemaException {
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();
		String serverURI = "http://services.soap.ws.messageservice.iam.axa.com/";
        
		MimeHeaders headers = soapMessage.getMimeHeaders();
		headers.addHeader("Content-Type", "text/xml");
		
		if (!soapConfig.getBypassAuthorization())
			headers.addHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword()));
		
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration("ser", serverURI);
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("messages", "ser");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("name");
        
        soapBodyElem1 =  addBodyElementsToRequest(soapBodyElem1, tokenInput, code);
        
		soapMessage.saveChanges();
		
		return soapMessage;
	}
	
	
	/**
	 * Addition of body element to the SOAP request
	 * @param SOAPElement soapBodyElem1
	 * @param TokenInput tokenInput
	 * @param String code
	 * @return soapBodyElem1
	 * @throws InvalidSchemaException
	 * @throws SOAPException
	 */
	private SOAPElement addBodyElementsToRequest(SOAPElement soapBodyElem1, TokenInput tokenInput, String code) throws InvalidSchemaException, SOAPException{
		
		String application = soapConfig.getApplication();
		String organization = soapConfig.getOrganization();
		String messageType = new String();
		String additionalAttributes = new String();
		String destination = new String();
		String message = new String();
		String sender = schemaListConfig.getSchemaItemConfig(tokenInput.getSchema()).getChannelConfig(tokenInput.getChannel().toString()).getSender();
		String language = schemaListConfig.getSchemaItemConfig(tokenInput.getSchema()).getChannelConfig(tokenInput.getChannel().toString()).getLanguage();
		
		if (tokenInput.getChannel().equals(ChannelEnum.mail)) {
			messageType = "EMAIL";
			additionalAttributes = "subject=" + "AXA OTP Generation";
			destination = tokenInput.getMail();
			message = "Use the code " + code + " for verification";
		}
		else if (tokenInput.getChannel().equals(ChannelEnum.sms)) {
			messageType = "SMS";
			additionalAttributes = "";
			destination = tokenInput.getPhone();
			message = "Use the code " + code + " for verification";
		} else if (tokenInput.getChannel().equals(ChannelEnum.voice)) {
			messageType = "VOICE";
			additionalAttributes = "OTP=" + code;
			destination = tokenInput.getPhone();
			message = "OTP";
			sender = destination;
		}
			
		soapBodyElem1.addChildElement("additionalAttributes").addTextNode(additionalAttributes);
        soapBodyElem1.addChildElement("application").addTextNode(application);
        soapBodyElem1.addChildElement("destination").addTextNode(destination);
        soapBodyElem1.addChildElement("language").addTextNode(language);
        soapBodyElem1.addChildElement("message").addTextNode(message);
        soapBodyElem1.addChildElement("messageType").addTextNode(messageType);
        soapBodyElem1.addChildElement("organization").addTextNode(organization);
        soapBodyElem1.addChildElement("sender").addTextNode(sender);
        
		return soapBodyElem1;
	}
	
	/**
	 * Allows to get a SOAP configuration variable from another class
	 * @return boolean
	 */
	public boolean isBypassedSendingToken(){
		return soapConfig.getBypassSendingToken();
	}
}
