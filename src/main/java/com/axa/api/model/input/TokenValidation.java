package com.axa.api.model.input;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * Model : Input expected containing MinimalTokenInput and the code to validate
 *
 */
public class TokenValidation extends MinimalTokenInput {

	@NotNull(message = "code is required")
	@ApiModelProperty(required = true)
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
