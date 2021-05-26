package br.com.siswbrasil.scaffoldjee;

import java.io.Serializable;

public class SourcePropertyDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String javaType;
	private String contextType;
	private String value;

	public SourcePropertyDetails(String name, String javaType, String contextType, String value) {
		super();
		this.name = name;
		this.javaType = javaType;
		this.contextType = contextType;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getJavaType() {
		return javaType;
	}

	public String getContextType() {
		return contextType;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "SourcePropertyDetails [name=" + name + ", javaType=" + javaType + ", contextType=" + contextType
				+ ", value=" + value + "]";
	}

}
