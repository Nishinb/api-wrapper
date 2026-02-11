package com.nishi.hello;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;


@Setter
@Getter
public class ProxyRequest {
    // Getters and Setters
    private String url;
	private Map<String, String> headers;
	private Object body;
	private int timeout = 30000; // Default to 30 seconds

	public ProxyRequest() {}

}
