package com.axa.api.configuration;

import com.axa.api.configuration.yml.OpenAMConfig;
import com.xebialabs.restito.builder.stub.StubHttp;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;

public class StubOpenAMConfig{
	
	private OpenAMConfig openAMConfig;
	private StubServer OpenAMServer;
	
	public StubOpenAMConfig(OpenAMConfig openAMConfig){
		this.openAMConfig = openAMConfig;
		OpenAMServer = new StubServer(Integer.parseInt(openAMConfig.getPort()));
	}
	
	public StubOpenAMConfig start(){
		stubMethod();
		OpenAMServer.start();
		return this;
	}
	
	public void stop(){
		OpenAMServer.stop();
	}
	
	private void stubMethod(){
		
		/** AuthenticateUser */
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "module"),
				Condition.parameter("authIndexValue", "scope_test_1"),
				Condition.parameter("noSession", "true"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", "login_test_1"),
				Condition.withHeader("X-OpenAM-Password", "Password1"))		
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"message\": \"Authentication Successful\",\n \"successUrl\": \"/openam/console\"\n}"));
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "module"),
				Condition.parameter("authIndexValue", "scope_test_1"),
				Condition.parameter("noSession", "true"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", "login_test_2"),
				Condition.withHeader("X-OpenAM-Password", "Password1"))		
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"message\": \"Authentication Successful\",\n \"successUrl\": \"/openam/console\"\n}"));
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "module"),
				Condition.parameter("authIndexValue", "scope_test_1"),
				Condition.parameter("noSession", "true"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", "login_test_3"),
				Condition.withHeader("X-OpenAM-Password", "Password1"))		
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"message\": \"Authentication Successful\",\n \"successUrl\": \"/openam/console\"\n}"));
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "module"),
				Condition.parameter("authIndexValue", "scope_test_1"),
				Condition.parameter("noSession", "true"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", "fake_uid"),
				Condition.withHeader("X-OpenAM-Password", "Password1"))		
		.then(	Action.unauthorized(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"code\": 401,\n \"reason\": \"Unauthorized\",\n \"message\": \"Authentication Failed\"\n}"));
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "module"),
				Condition.parameter("authIndexValue", "scope_test_1"),
				Condition.parameter("noSession", "true"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", "login_test_1"),
				Condition.withHeader("X-OpenAM-Password", "fake_password"))		
		.then(	Action.unauthorized(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"code\": 401,\n \"reason\": \"Unauthorized\",\n \"message\": \"Authentication Failed\"\n}"));
		
		/** GetAdminToken */
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "service"),
				Condition.parameter("authIndexValue", openAMConfig.getAdminServiceChain()),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", openAMConfig.getAdminUser()),
				Condition.withHeader("X-OpenAM-Password", openAMConfig.getAdminPwd()))		
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"tokenId\": \"token_test\",\n \"successUrl\": \"/openam/console\"\n}"));
		
		
		/**
		 * RetrieveUserInformations
		 * Scope: scope_test_1
		 */
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_1\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_1\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 1,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_2\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [],\n  \"resultCount\": 0,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_3\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_3\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    },\n    {\n      \"username\": \"uid_test_2\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_3\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 2,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_4\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        null\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        null\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_1\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 1,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"fake_uid\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [],\n  \"resultCount\": 0,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		/**
		 * RetrieveUserInformations
		 * Scope: scope_test_2
		 */
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_1\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_1\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 1,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_2\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [],\n  \"resultCount\": 0,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_3\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_3\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    },\n    {\n      \"username\": \"uid_test_2\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_3\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 2,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_4\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        null\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        null\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_1\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 1,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"fake_uid\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [],\n  \"resultCount\": 0,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		/** Session expired */
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/sessions"),
				Condition.parameter("_action", "getTimeLeft"),
				Condition.parameter("tokenId", "token_test"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"maxtime\": 7500\n}"));
	}
}
