# Selective-Change-Management-of-Project-Repository
Project to reduce manual work of removing each components from your "src" repository which are not included in "package.xml" having components for the current release. 

Procedure:
1. Copy Paste your “src” metadata code into the "original_src" folder.
2. Update the "package.xml" with your selective deployment metadata components.
3. The "srcContentXML" is a xml file which contains, folder names linked to their Metadata Names described in "package.xml". Please don’t  update it unless really required.
4. Open command prompt in the root folder.
5. Run the following command,
        "  java com.SelectiveDeploymentRepoManager  "
7. As the process completes “selective_src” folder will have the updated repository metadata code.
