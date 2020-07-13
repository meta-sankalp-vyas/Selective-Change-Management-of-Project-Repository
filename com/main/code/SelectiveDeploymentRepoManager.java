package com;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.lang.String;

  
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
  
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
  
public class SelectiveDeploymentRepoManager {
	
	public final String packageXMLFilePath = ".\\package.xml";
	public final String packageContentNameToken = "name";
	public final String packageContentMemberToken = "members";
	
	public final String srcContentXMLFilePath = ".\\srcContentXML.xml";
	public final String srcFolderContentNameToken = "name";
	public final String srcFolderContentFolderNameToken = "folderName";
	
	public final String srcFolderName = "original_src";
	public final String selectiveSrcFolderName = "selective_src";
	public final String projectPath = ".\\";
	
	public final String nonExistingFolderToken = "N/A";

	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		SelectiveDeploymentRepoManager sdrmObj = new SelectiveDeploymentRepoManager();
		sdrmObj.startProcess();
	}
	
	private void startProcess() throws ParserConfigurationException, SAXException, IOException {
		if(deleteDirectoryContents(projectPath + selectiveSrcFolderName)) {
			System.out.println("Getting Required Info.....");
			Map<String, Set<String>> packageXMLContent = getListOfContentOfNewPackageXML();
			Map<String, String> srcContentXML = getListOfContentOfSrcContentXML();
			System.out.println("Extracting Related Package.xml Folders into Selective Folders....");
			createValidDirectoryCopiesFromOriginalSrcToSelectiveSrc(srcContentXML, packageXMLContent);
			System.out.println("Deleting not mentioned Components in Package.xml....");
			deleteFilesWhichAreNotIncludedInPackageXMLFromSelectiveSrcFolder(srcContentXML, packageXMLContent);
			System.out.println("Completed.");
		} else {
			System.out.println("Couldn\'t delete selective_src folder contents, before starting the process. Please do the manual effort");
		}
	}
	
	// Returns a Map on the basis of New Package.xml read.
	private Map<String, Set<String>> getListOfContentOfNewPackageXML() throws ParserConfigurationException,
		  SAXException, IOException {
		//Get Document Builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
  
		// Load the input XML document, parse it and return an instance of the
		// Document class.
		Document document = builder.parse(new File(packageXMLFilePath));
  
		Map<String, Set<String>> packageXMLContent = new HashMap<String, Set<String>>();
		NodeList nodeList = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element elem = (Element) node;
  
				// Get the value of all sub-elements.
				String name = "Blank";
				NodeList nameNodeList = elem.getElementsByTagName(packageContentNameToken);
										
				if(nameNodeList != null && nameNodeList.getLength() > 0){
					name = nameNodeList.item(0).getChildNodes().item(0).getNodeValue();
				}
				NodeList members = elem.getElementsByTagName(packageContentMemberToken);
				Set<String> memberNames = new HashSet<String>();
				for (int j = 0; j < members.getLength(); j++) {
					Node nodeChild = members.item(j);
					Element element = (Element) nodeChild;
					memberNames.add(element.getChildNodes().item(0).getNodeValue());
				}
				packageXMLContent.put(name,memberNames);
			}
		}
		return packageXMLContent;
	}
	
	// Returns a Map on the basis of SrcContent.xml read.
	private Map<String, String> getListOfContentOfSrcContentXML() throws ParserConfigurationException,
		  SAXException, IOException {
		//Get Document Builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
  
		// Load the input XML document, parse it and return an instance of the
		// Document class.
		Document document = builder.parse(new File(srcContentXMLFilePath));
  
		Map<String, String> srcContentXML = new HashMap<String, String>();
		NodeList nodeList = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element elem = (Element) node;
				// Get the value of all sub-elements.
				String name = "Blank";
				String folderName = "";
				NodeList nameNodeList = elem.getElementsByTagName(srcFolderContentNameToken);                     
				if(nameNodeList != null && nameNodeList.getLength() > 0){
					name = nameNodeList.item(0).getChildNodes().item(0).getNodeValue();
				}
				NodeList folderNameNodeList = elem.getElementsByTagName(srcFolderContentFolderNameToken);
				if(folderNameNodeList != null && folderNameNodeList.getLength() > 0){
					folderName = folderNameNodeList.item(0).getChildNodes().item(0).getNodeValue();
				}
				srcContentXML.put(name,folderName);
			}
		}
		return srcContentXML;
	}
	
	private Boolean createValidDirectoryCopiesFromOriginalSrcToSelectiveSrc(Map<String, String> srcContentXML, Map<String, Set<String>> packageXML) throws IOException {
		Boolean flag = true;
		Set<String> foldersToBeTransfered = new HashSet<String>();
		for(String packageComponentName : packageXML.keySet()){
			if(srcContentXML.containsKey(packageComponentName) && srcContentXML.get(packageComponentName)!= nonExistingFolderToken) {
				foldersToBeTransfered.add(srcContentXML.get(packageComponentName));
			}
		}
		for(String folderNameToBeTransfered : foldersToBeTransfered) {
			if(folderNameToBeTransfered != null && folderNameToBeTransfered.trim() != ""){
				String srcPath = projectPath + srcFolderName + "\\" + folderNameToBeTransfered;
				String destPath = projectPath + selectiveSrcFolderName + "\\" + folderNameToBeTransfered;
				Boolean tempFlag = true;
				tempFlag = copyOneDirectoryToAnotherLocation(srcPath, destPath);
				if(flag) {
					flag = tempFlag;
				}
			}
		}
		return flag;
	}
	
	private Boolean deleteFilesWhichAreNotIncludedInPackageXMLFromSelectiveSrcFolder(Map<String, String> srcContentXML, Map<String, Set<String>> packageXML){
		Boolean flag = true;
		for(String packageXMLComponent : packageXML.keySet()){
			Boolean isSuccess = true;
			if(srcContentXML.containsKey(packageXMLComponent) && srcContentXML.get(packageXMLComponent) != nonExistingFolderToken){
				String folderName = srcContentXML.get(packageXMLComponent);
				String directoryPath = projectPath + selectiveSrcFolderName + "\\" + folderName;
				Set<String> fileNamesToBeIncluded = packageXML.get(packageXMLComponent);
				if(fileNamesToBeIncluded != null && fileNamesToBeIncluded.size() == 1 && fileNamesToBeIncluded.contains("*")) {
					System.out.println("All components are included for directory: " + folderName);
				} else if (fileNamesToBeIncluded != null && fileNamesToBeIncluded.size() > 0) {
					System.out.println("Syncing " + folderName + "files with package.xml....");
					isSuccess = deleteNotSelectedFileFromDirectory(directoryPath, fileNamesToBeIncluded, 0);
				}
			}
			if(flag){
				flag = isSuccess;
			}
		}
		return flag;
	}
	
	private Boolean deleteNotSelectedFileFromDirectory (String directoryPath, Set<String> directoryContentToBeIncluded, Integer nestedCallNumber) {
		Scanner sc= new Scanner(System.in);
		Boolean flag = true;
		File file = new File(directoryPath);
		File[] listOfFiles = file.listFiles();
		if(listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				Boolean isSuccess = true;
				String fileName = listOfFiles[i].getName();
				if (listOfFiles[i].isFile()) {
					String[] arrOfFileName = fileName.split("\\.");
					if(nestedCallNumber > 0 && !directoryPath.contains("aura")){
						String tempDirectoryPath = directoryPath.replace("\\","/");
						String[] directoryPathArray = tempDirectoryPath.split("/",4);
						fileName = directoryPathArray[3] + "/" + arrOfFileName[0];
					} else {
						fileName =  arrOfFileName[0];
					}
					//aura components are not specifically mentioned in the package.xml that's why if the main name matches
					if(directoryContentToBeIncluded.contains(fileName) && directoryPath.contains("aura") && nestedCallNumber == 1){
						break;
					}
					if(!directoryContentToBeIncluded.contains(fileName)){
						isSuccess = new File(listOfFiles[i].toString()).delete();					
					}
				} else {
					//recursive call for nested directories specifically for reports and dashboards
					deleteNotSelectedFileFromDirectory(directoryPath + "\\" + fileName, directoryContentToBeIncluded, nestedCallNumber + 1);
					if(!directoryContentToBeIncluded.contains(fileName)){
						isSuccess = new File(listOfFiles[i].toString()).delete();					
					}
				}
				if(flag){
					flag = isSuccess;
				}
			}
		}
		return flag;
	}
	
	private Boolean copyOneDirectoryToAnotherLocation (String sourcePath, String destinationPath)throws IOException {
		Boolean flag = true;
		File srcFolder = new File(sourcePath);
		File destFolder = new File(destinationPath);

		//make sure source exists
		if(!srcFolder.exists()){
			System.out.println("Directory does not exist.");
		}else{
			try{
				copyFolder(srcFolder,destFolder);
			}catch(IOException e){
				e.printStackTrace();
				flag = false;
			}
		}
		return flag;
	}
	
	private void copyFolder(File src, File dest)throws IOException{
		if(src.isDirectory()){
			//if directory not exists, create it
			if(!dest.exists()){
			   dest.mkdir();
			   System.out.println("Directory copied from "+ src + "  to " + dest);
			}
			//list all the directory contents
			String files[] = src.list();
			for (String file : files) {
			   //construct the src and dest file structure
			   File srcFile = new File(src, file);
			   File destFile = new File(dest, file);
			   //recursive copy
			   copyFolder(srcFile,destFile);
			}
		}else{
			//if file, then copy it
			//Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			//copy the file content in bytes
			while ((length = in.read(buffer)) > 0){
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
		}
	}
	
	private Boolean createDirectory(String directoryPath, String directoryName) {
		try {
			File file = new File(directoryPath + directoryName);
			file.createNewFile();
			Boolean flag = file.mkdir();
			return flag;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private Boolean deleteDirectoryContents(String directoryPath) {
		Boolean flag = true;
		File file = new File(directoryPath);
		File[] listOfFiles = file.listFiles(); 
		for (int i = 0; i < listOfFiles.length; i++) {
			Boolean isSuccess = true;
			if (listOfFiles[i].isFile()) {
				String fileName = listOfFiles[i].getName();
				isSuccess = new File(listOfFiles[i].toString()).delete();
			} else {
				deleteDirectoryContents(directoryPath + "\\" + listOfFiles[i].getName());
				isSuccess = new File(listOfFiles[i].toString()).delete();
			}
			if(flag){
				flag = isSuccess;
			}
		}
		return flag;
	}
}
