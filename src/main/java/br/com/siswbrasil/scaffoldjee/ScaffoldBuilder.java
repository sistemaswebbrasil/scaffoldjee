package br.com.siswbrasil.scaffoldjee;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import br.com.siswbrasil.scaffoldjee.exception.ScaffoldBuilderException;
import br.com.siswbrasil.scaffoldjee.exception.ScaffoldBuilderNotFoudException;

public class ScaffoldBuilder {

	public List<SourceProperty> scanSourceFiles(String sourceDirectory, String sourceType)
			throws ScaffoldBuilderNotFoudException {
//		System.out.println("==========================================================");
//		System.out.println(String.format("sourceDirectory: %s",sourceDirectory));
//		System.out.println(String.format("sourceType: %s",sourceType));
//		System.out.println("==========================================================");
		List<SourceProperty> sourceFiles = new ArrayList<SourceProperty>();
		File actual = new File(sourceDirectory);
		if (!actual.exists()) {
			throw new ScaffoldBuilderNotFoudException(String.format("Source directory %s not found", sourceDirectory));
		}

		for (File f : actual.listFiles()) {
			SourceProperty object = new SourceProperty();
			object.setId(UUID.randomUUID().toString());
			object.setName(f.getName().replace(".java", ""));
			object.setType(sourceType);
			object.setPath(f.getAbsolutePath());
			sourceFiles.add(object);
		}

		return sourceFiles;
	}

	public OutputGenereate generateLabels(SourceProperty selected, String destinationFile)
			throws ScaffoldBuilderException {
		String objectContent = "";

		if (destinationFile == null || destinationFile.isEmpty()) {
			throw new ScaffoldBuilderException("Destination path not informed");
		}

		List<String> lines = readFile(destinationFile, true);
		List<String> newLines = lines;
		List<SourcePropertyDetails> properties = selected.getDetails();

		for (SourcePropertyDetails item : properties) {
			if (!item.getContextType().equals("label") && !item.getContextType().equals("field")) {
				continue;
			}
			String formattedTitle = splitCamelCase(item.getName());
			String newLine = String.format("%s.%s=%s", selected.getName().toLowerCase(), item.getName().toLowerCase(),
					formattedTitle);
			Boolean fieldExist = false;
			for (String line : lines) {
				if (line.substring(0, line.indexOf("=")).equalsIgnoreCase(newLine.substring(0, newLine.indexOf("=")))) {
					fieldExist = true;
				}
			}
			if (fieldExist == false) {
				newLines.add(newLine);
			}
		}
		newLines.sort((p1, p2) -> p1.compareTo(p2));
		objectContent = buildObjectContent(newLines);
		return new OutputGenereate(objectContent, destinationFile);
	}

	public void writeInFile(OutputGenereate outputGenereate) throws ScaffoldBuilderException {
		if (outputGenereate == null) {
			throw new ScaffoldBuilderException("Object for generation was not reported");
		}
		if (outputGenereate != null && outputGenereate.getOutput() == null || outputGenereate.getOutput().isEmpty()) {
			throw new ScaffoldBuilderException("Content this emptiness and nothing will be created");
		}
		if (outputGenereate != null && outputGenereate.getDetination() == null
				|| outputGenereate.getDetination().isEmpty()) {
			throw new ScaffoldBuilderException("Uninformed destination path");
		}

		String fileAux = new File(outputGenereate.getDetination()).getParent();
		File baseViewFolderPathDir = new File(fileAux);
		if (!baseViewFolderPathDir.exists()) {
			baseViewFolderPathDir.mkdirs();
		}
		try {
			FileWriter fileWriter = new FileWriter(outputGenereate.getDetination());
			fileWriter.write(outputGenereate.getOutput());
			fileWriter.close();
		} catch (IOException e) {
			throw new ScaffoldBuilderException("Failure to write new content", e);
		}
	}

	private String buildObjectContent(List<String> lines) {
		String objectContent = "";
		for (String line : lines) {
			objectContent += line + "\n";
		}
		return objectContent;
	}

	private String splitCamelCase(String s) {
		return s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
	}

	public SourceProperty readProperties(SourceProperty selected) throws ScaffoldBuilderNotFoudException {
		List<String> lines = readFile(selected.getPath(), true);
		List<SourcePropertyDetails> details = scanLines(lines, selected);
		selected.setDetails(details);
		return selected;
	}

	private List<SourcePropertyDetails> scanLines(List<String> lines, SourceProperty selected) {
		List<SourcePropertyDetails> details = new ArrayList<>();
		boolean selectedIsId = false;
		for (String line : lines) {
			String selectedName = null;
			String selectedType = null;
			if (line.contains(";")) {
				line = line.substring(0, line.indexOf(";"));
			}

			if (line.startsWith("package")) {
				details.add(new SourcePropertyDetails(String.format("%sPackage", selected.getType()), "package",
						"packageName", line.split("package ")[1].trim()));
			}

			String[] partLine = line.split(" ");
			if (selected.getType().equalsIgnoreCase("entity")) {
				if (partLine[0].contentEquals("@Id") || partLine[0].contentEquals("@EmbeddedId")) {
					selectedIsId = true;
				}
				if (partLine[0].contentEquals("private") && !partLine[1].contentEquals("static")) {
					selectedType = partLine[1];
					selectedName = partLine[2];
					details.add(new SourcePropertyDetails(selectedName, selectedType, "field", null));
					if (selectedIsId == true) {
						details.add(new SourcePropertyDetails(selectedName, selectedType, "key", null));
					}
					selectedIsId = false;
				}
			}
		}
		return details;
	}

	public List<String> readFile(String objectPath, Boolean clear) throws ScaffoldBuilderNotFoudException {
		List<String> rowsArray = new ArrayList<String>();
		Path path = Paths.get(objectPath);
		try {
			if (clear) {
				Files.lines(path).map(s -> s.trim()).filter(s -> !((String) s).isEmpty())
						.forEach(s -> rowsArray.add(s));
			} else {
				Files.lines(path).forEach(s -> rowsArray.add(s));
			}
		} catch (IOException e) {
			throw new ScaffoldBuilderNotFoudException(String.format("Failed to read the file %s", objectPath), e);
		}
		return rowsArray;
	}

	public OutputGenereate generateRepository(SourceProperty selected, String baseRepositoryPath,
			String templateRepositoryPath) throws ScaffoldBuilderException {
		
		if (baseRepositoryPath == null || baseRepositoryPath.isEmpty()) {
			throw new ScaffoldBuilderException("Base Repository path not informed");
		}
		
		if (templateRepositoryPath == null || templateRepositoryPath.isEmpty()) {
			throw new ScaffoldBuilderException("Template Repository path not informed");
		}	

		String repositoryBasePackage = null;
		String entityPackage = null;
		String entity = selected.getName();
		String repository = String.format("%s%s", entity,"Repository");
		String repositoryPath = String.format("%s/%s.java", baseRepositoryPath,repository);  
		String idType = null ;

		for (SourcePropertyDetails item : selected.getDetails()) {			
			if (item.getContextType().equals("packageName")) {
				entityPackage = item.getValue();
				repositoryBasePackage = entityPackage.subSequence(0, entityPackage.lastIndexOf(".")) + ".repository";
			}
			if (item.getContextType().equals("key")) {
				idType = item.getJavaType();
			}			
		}
		
		List<String> sourceProperties = Arrays.asList("${repositoryBasePackage}","${entityPackage}","${entity}","${repository}","${id-type}");		
		List<String> replacedProperties = Arrays.asList(repositoryBasePackage,entityPackage,entity,repository,idType);	
		
		String objectContent = replaceProperties(templateRepositoryPath, sourceProperties, replacedProperties);
		return new OutputGenereate(objectContent, repositoryPath);
	}
	
	private String replaceProperties(String templateFile,List<String> sourceProperties,List<String> replacedProperties) throws ScaffoldBuilderNotFoudException {
		List<String> lines = readFile(templateFile, false);		
		List<String> newLines = new ArrayList<String>();
		for (String line : lines) {
			
			for (int i = 0; i < sourceProperties.size(); i++) {
				if (line.contains(sourceProperties.get(i))) {
					line = line.replace(sourceProperties.get(i), replacedProperties.get(i));
				}				
			}
			newLines.add(line);			
		}
		return buildObjectContent(newLines);		
	}

}
