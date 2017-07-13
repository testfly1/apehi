package com.axa.api.configuration;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.axa.api.configuration.yml.SoapConfig;
import com.unboundid.util.Base64;
import com.xebialabs.restito.builder.stub.StubHttp;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;

public class StubSOAPConfig {

	private SoapConfig soapConfig;
	private StubServer SOAPServer;
	
	public StubSOAPConfig(SoapConfig soapConfig){
		this.soapConfig = soapConfig;
		SOAPServer = new StubServer(soapConfig.getPort());
	}
	
	public StubSOAPConfig start() throws SAXException, IOException, ParserConfigurationException{
		stubMethod();
		SOAPServer.start();
		return this;
	}
	
	public void stop(){
		SOAPServer.stop();
	}
	
	private void stubMethod() throws SAXException, IOException, ParserConfigurationException{
		
		SOAPServer = new StubServer(new Integer(soapConfig.getPort())).run();
		
		/** SOAP OK */
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword())),
				Condition.withHeader("Content-Type", "text/xml; charset=utf-8"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
											"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)" + 
											"<application>" + soapConfig.getApplication() + "</application>" + 
											"<destination>" + "(phone_test|mail_test)" + "</destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(EMAIL|SMS|VOICE)" + "</messageType>" + 
											"<organization>" + soapConfig.getOrganization() + "</organization>" + 
											"<sender>" + "(sender_test|phone_test)" + "</sender>" + 
										"</name>" + 
									"</ser:messages>" + 
								"</SOAP-ENV:Body>" + 
							"</SOAP-ENV:Envelope>")))
		.then(	Action.ok(),
				Action.contentType("text/xml"),
				Action.stringContent(
						"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>" +
							"<soap:Body>" +
								"<ns2:messagesResponse xmlns:ns2=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
									"<return>" +
										"<providerName>Infinite</providerName>" +
										"<status>SEND_SUCCESSFUL</status>" +
									"</return>" +
								"</ns2:messagesResponse>" +
							"</soap:Body>" +
						"</soap:Envelope>"));
		
		/** SOAP additionalAttributes NOK */
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword())),
				Condition.withHeader("Content-Type", "text/xml; charset=utf-8"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
											"<additionalAttributes" + "(?!(/|>OTP=[0-9]{6}</additionalAttributes|>subject=AXA OTP Generation</additionalAttributes)).*" + ">" + 
											"<application>" + soapConfig.getApplication() + "</application>" + 
											"<destination>" + "(phone_test|mail_test)" + "</destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(EMAIL|SMS|VOICE)" + "</messageType>" + 
											"<organization>" + soapConfig.getOrganization() + "</organization>" + 
											"<sender>" + "(sender_test|phone_test)" + "</sender>" + 
										"</name>" + 
									"</ser:messages>" + 
								"</SOAP-ENV:Body>" + 
							"</SOAP-ENV:Envelope>")))
		.then(	Action.status(org.glassfish.grizzly.http.util.HttpStatus.BAD_REQUEST_400),
				Action.contentType("text/xml"),
				Action.stringContent(
						"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>" +
							"<soap:Body>" +
								"<ns2:messagesResponse xmlns:ns2=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
									"<return>" +
										"<providerName>Message Service</providerName>" +
										"<status>SEND_UNSUCCESSFUL_BAD_REQUEST</status>" +
									"</return>" +
								"</ns2:messagesResponse>" +
							"</soap:Body>" +
						"</soap:Envelope>"));
	
		/** SOAP destination NOK */
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword())),
				Condition.withHeader("Content-Type", "text/xml; charset=utf-8"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
										"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)"  +
											"<application>" + soapConfig.getApplication() + "</application>" + 
											"<destination>" + "(?!(phone_test|mail_test)<).*" + "/destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(EMAIL|SMS|VOICE)" + "</messageType>" + 
											"<organization>" + soapConfig.getOrganization() + "</organization>" + 
											"<sender>" + "(sender_test|phone_test)" + "</sender>" + 
										"</name>" + 
									"</ser:messages>" + 
								"</SOAP-ENV:Body>" + 
							"</SOAP-ENV:Envelope>")))
		.then(	Action.status(org.glassfish.grizzly.http.util.HttpStatus.BAD_REQUEST_400),
				Action.contentType("text/xml"),
				Action.stringContent(
						"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>" +
							"<soap:Body>" +
								"<ns2:messagesResponse xmlns:ns2=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
									"<return>" +
										"<providerName>Message Service</providerName>" +
										"<status>SEND_UNSUCCESSFUL_BAD_REQUEST</status>" +
									"</return>" +
								"</ns2:messagesResponse>" +
							"</soap:Body>" +
						"</soap:Envelope>"));
		
		/** SOAP message NOK */
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword())),
				Condition.withHeader("Content-Type", "text/xml; charset=utf-8"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
										"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)" + 
											"<application>" + soapConfig.getApplication() + "</application>" + 
											"<destination>" + "(phone_test|mail_test)" + "/destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(?!(Use the code [0-9]{6} for verification|OTP)<).*" + "/message>" + 
											"<messageType>" + "(EMAIL|SMS|VOICE)" + "</messageType>" + 
											"<organization>" + soapConfig.getOrganization() + "</organization>" + 
											"<sender>" + "(sender_test|phone_test)" + "</sender>" + 
										"</name>" + 
									"</ser:messages>" + 
								"</SOAP-ENV:Body>" + 
							"</SOAP-ENV:Envelope>")))
		.then(	Action.status(org.glassfish.grizzly.http.util.HttpStatus.BAD_REQUEST_400),
				Action.contentType("text/xml"),
				Action.stringContent(
						"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>" +
							"<soap:Body>" +
								"<ns2:messagesResponse xmlns:ns2=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
									"<return>" +
										"<providerName>Message Service</providerName>" +
										"<status>SEND_UNSUCCESSFUL_BAD_REQUEST</status>" +
									"</return>" +
								"</ns2:messagesResponse>" +
							"</soap:Body>" +
						"</soap:Envelope>"));
		
		/** SOAP messageType NOK */
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword())),
				Condition.withHeader("Content-Type", "text/xml; charset=utf-8"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
											"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)" + 
											"<application>" + soapConfig.getApplication() + "</application>" + 
											"<destination>" + "(?!(phone_test|mail_test)<).*" + "/destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(?!(EMAIL|SMS|VOICE)<).*" + "/messageType>" + 
											"<organization>" + soapConfig.getOrganization() + "</organization>" + 
											"<sender>" + "(sender_test|phone_test)" + "</sender>" + 
										"</name>" + 
									"</ser:messages>" + 
								"</SOAP-ENV:Body>" + 
							"</SOAP-ENV:Envelope>")))
		.then(	Action.status(org.glassfish.grizzly.http.util.HttpStatus.BAD_REQUEST_400),
				Action.contentType("text/xml"),
				Action.stringContent(
						"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>" +
							"<soap:Body>" +
								"<ns2:messagesResponse xmlns:ns2=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
									"<return>" +
										"<providerName>Message Service</providerName>" +
										"<status>SEND_UNSUCCESSFUL_BAD_REQUEST</status>" +
									"</return>" +
								"</ns2:messagesResponse>" +
							"</soap:Body>" +
						"</soap:Envelope>"));
		
		/** SOAP sender NOK */
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword())),
				Condition.withHeader("Content-Type", "text/xml; charset=utf-8"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
											"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)" + 
											"<application>" + soapConfig.getApplication() + "</application>" + 
											"<destination>" + "(?!(phone_test|mail_test)<).*" + "/destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(EMAIL|SMS|VOICE)" + "/messageType>" + 
											"<organization>" + soapConfig.getOrganization() + "</organization>" + 
											"<sender>" + "(?!sender_test|phone_test<).*" + "/sender>" + 
										"</name>" + 
									"</ser:messages>" + 
								"</SOAP-ENV:Body>" + 
							"</SOAP-ENV:Envelope>")))
		.then(	Action.status(org.glassfish.grizzly.http.util.HttpStatus.BAD_REQUEST_400),
				Action.contentType("text/xml"),
				Action.stringContent(
						"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>" +
							"<soap:Body>" +
								"<ns2:messagesResponse xmlns:ns2=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
									"<return>" +
										"<providerName>Message Service</providerName>" +
										"<status>SEND_UNSUCCESSFUL_BAD_REQUEST</status>" +
									"</return>" +
								"</ns2:messagesResponse>" +
							"</soap:Body>" +
						"</soap:Envelope>"));
		
	}
}
