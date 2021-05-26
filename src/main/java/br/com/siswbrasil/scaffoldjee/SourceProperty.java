package br.com.siswbrasil.scaffoldjee;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SourceProperty implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String name;
	private String type;
	private String path;

	private List<SourcePropertyDetails> details = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<SourcePropertyDetails> getDetails() {
		return details;
	}

	public void setDetails(List<SourcePropertyDetails> details) {
		this.details = details;
	}

	@Override
	public String toString() {
		return "SourceProperty [id=" + id + ", name=" + name + ", type=" + type + ", path=" + path + ", details="
				+ details + "]";
	}
	
	

}
