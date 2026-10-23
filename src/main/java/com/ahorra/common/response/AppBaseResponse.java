package com.ahorra.common.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class AppBaseResponse {

    /**
     * -- GETTER --
     *  get the value of the HTTP status code for the response payload
     *
     *
     * -- SETTER --
     *  set the value of the HTTP status code for the response payload
     *
     */
    private int statusCode;

    /**
     * -- GETTER --
     *  set the description of the result of current http request
     *
     *
     * -- SETTER --
     *  set the description of the result of current http request
     *
     */
    private String description;

    /**
     * -- GETTER --
     *  get the data for the response payload
     *
     *
     * -- SETTER --
     *  set the data for the response payload
     *
     */
    private Object data;

    /**
     * -- GETTER --
     *  get the errors that occurred during the current request
     *
     *
     * -- SETTER --
     *  set the errors that occurred during the current request
     *
     */
    private Object errors;

	private Map<String, Object> additionalData;

	public AppBaseResponse() {
	}

	public AppBaseResponse(int statusCode) {
		super();
		this.statusCode = statusCode;
	}

	public AppBaseResponse(int statusCode, String description) {
		super();
		this.statusCode = statusCode;
		this.description = description;
	}

	public AppBaseResponse(int statusCode, String description, Object data) {
		super();
		this.statusCode = statusCode;
		this.description = description;
		this.data = data;
	}

	public AppBaseResponse(int statusCode, String description, Object data, Map<String, Object> additionalData) {
		this.statusCode = statusCode;
		this.description = description;
		this.data = data;
        this.additionalData = additionalData;
    }

}