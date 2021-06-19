# mapforce-runner-azure-function

## Objective
To have 1 common azure function that can run dynamically any mapforce jar as follow:
![Objective](images/1-box-diagram.png)

## Sample
![Objective](images/2-box-diagram.png)

## Notes
This project is :
- a proof of concept
- gives an azure function, common function run mapforce jars
- works for simple mapping where there is  1 input component & 1 output component as follow

![Simple 1 input 1 output mapping](images/0-mapforce-simple-mapping.png)


# Steps to run

1. Create Azure required resources: 
   - Resource group
   - Storage account + blob container
   - Blank Java Function App
   
2. Set up local development environment:
   - IDE
   - JDK 11
   - Azure CLI 2.23
   - Azure Core Tools 3.0.x
   - Maven 3
   - Ant (for creating mapforce jars)
   - REST Client (Postman/Soap UI, for testing)

4. Clone this repository
   
5. Copy the given jar **contacts-csv-to-xml-1.0.jar** to azure blob storage created in step 1, take note of the:
   - Blob container name : mapforcejar-container
   - Blob path : contacts-csv-to-xml/contacts-csv-to-xml-1.0.jar
   
6. Update local.settings.json, change this setting:
   
        "MapforceBlobConnectionString" : "update with storage account connection string"
   
7. To test locally, run:
   
        mvn clean package
        mvn azure-functions:run
   
8. Once function is running locally, use the sample input data (input1.csv) and rest client to send http post data to following endpoint:
   
        curl --location --request POST 'http://localhost:7071/api/mapforcerunner?mapforcejarpath=contacts-csv-to-xml/contacts-csv-to-xml-1.0.jar&mapforceclassname=com.mapforce.MappingMapTocontacts' \
        --header 'Content-Type: text/plain' \
        --data-raw 'id,name,email
        1,John,john@singpost.com
        2,Jane,jane@qs.com
        3,Mark,mark@cp.com.au
        4,Kat,kat@qs.co.jp'
   

9. To deploy to Azure, update pom.xml change "azure-functions-maven-plugin" configuration accordingly:

         <appName>change-this-to-what-was-created-in-step-1</appName>
         <resourceGroup>change-this-to-what-was-created-in-step-1</resourceGroup>
         
10. Then run:
    
         mvn azure-functions:deploy
    
11. Go to Azure Portal > function app > configurations, add these settings:
    
        MapforceBlobConnectionString : connection_string_to_blob
        MapforceBlobContainerName : mapforcejar-container

12. Test again using the endpoint provided by azure function app

        https://{functionAppName}.azurewebsites.net/api/mapforcerunner

> At the end of step 10, the endpoint url will be shown in console. 
> You can also get the endpoint url from Azure Portal.




