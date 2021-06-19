package net.funbox987;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

public class Function {

    public static final String CLASS_COM_ALTOVA_IO_INPUT = "com.altova.io.Input";
    public static final String CLASS_COM_ALTOVA_IO_OUTPUT = "com.altova.io.Output";

    public static final String CLASS_COM_ALTOVA_IO_READERINPUT = "com.altova.io.ReaderInput";
    public static final String CLASS_COM_ALTOVA_IO_WRITEROUTPUT = "com.altova.io.WriterOutput";

    public static final String METHOD_RUN = "run";

    //private String mapforceJarPath = null;
    private String mapforceClassname = null;

    private Class classMapforceGenerated = null;
    private Class classComAltovaIoInput = null;
    private Class classComAltovaIoOutput = null;
    private Class classComAltovaIoReaderInput = null;
    private Class classComAltovaIoWriterOutput = null;

    private Constructor consComAltovaIoReaderInput = null;
    private Constructor consComAltovaIoWriterOutput = null;

    private Method mapforceMethod = null;
    private Object mapforceObject = null;

    private String blobConnString = System.getenv("MapforceBlobConnectionString");
    private String blobContainerName = System.getenv("MapforceBlobContainerName");

    private String mapforceJarLocalTempFilePath = System.getProperty("java.io.tmpdir");

    private Logger logger;

    @FunctionName("mapforcerunner")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        logger = context.getLogger();

        // Parse query parameter
        final String mapforceJarPath = request.getQueryParameters().get("mapforcejarpath");
        mapforceClassname = request.getQueryParameters().get("mapforceclassname");
        final String body = request.getBody().orElse("");
        String result = "";

        mapforceJarLocalTempFilePath += mapforceJarPath;
        if (mapforceJarPath.contains("/")) {
          mapforceJarLocalTempFilePath = mapforceJarLocalTempFilePath.replace('/','_');
        }


        logger.info("Incoming request");
        logger.info("mapforceJarLocalTempFilePath = " + mapforceJarLocalTempFilePath);

        if (mapforceClassname == null || mapforceClassname == "") {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass in mapforce class name").build();
        } else if (mapforceJarPath == null || mapforceJarPath == "") {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass in mapforce jar path.").build();        }
        else if (body == "") {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass in data.").build();
        } else {

            try {
                downloadJarFromBlob(mapforceJarPath);
            } catch (Exception e) {

                result = "Unable to load jar " + mapforceJarPath + " from blob container " + blobContainerName;
                logger.severe(result);
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(result).build();
            }

            try {
                File f = new File(mapforceJarLocalTempFilePath);
                URL u = f.toURI().toURL();
                loadMapforceClassesFromJar(f.toURI().toURL());
            } catch (Exception e) {
                result = "Unable to load classes from " + mapforceJarLocalTempFilePath + " with error " + e.getMessage();

                logger.severe(result);
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(result).build();
            }

            try {

                // at this point, classes are fully loaded
                // so do the transform

                StringReader readerInputData = new StringReader(body);
                StringWriter writerOutputData = new StringWriter();

                Object objInputData = consComAltovaIoReaderInput.newInstance(readerInputData);
                Object objOutputData = consComAltovaIoWriterOutput.newInstance(writerOutputData);

                mapforceMethod.invoke(mapforceObject, new Object[]{objInputData, objOutputData });

                result = writerOutputData.getBuffer().toString();

                return request.createResponseBuilder(HttpStatus.OK).body(result).build();

            } catch (Exception e) {
                logger.severe("Unable to transform data");
                result = "Transform failed due to " + e.getMessage()+ ". Please check logs for more details.";
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(result).build();
            }


        }
    }

    private void loadMapforceClassesFromJar(URL mapforceJarPathAsUrl) throws Exception  {

        logger.info("Loading classes...");
        logger.info("mapforceJarPath = " + mapforceJarPathAsUrl);
        logger.info("mapforceClassname = " + mapforceClassname);

        URLClassLoader clazzLoader = URLClassLoader.newInstance(new URL[] { mapforceJarPathAsUrl });

        classMapforceGenerated = clazzLoader.loadClass(mapforceClassname);
        classComAltovaIoInput = clazzLoader.loadClass(CLASS_COM_ALTOVA_IO_INPUT);
        classComAltovaIoOutput = clazzLoader.loadClass(CLASS_COM_ALTOVA_IO_OUTPUT);

        classComAltovaIoReaderInput = clazzLoader.loadClass(CLASS_COM_ALTOVA_IO_READERINPUT);
        classComAltovaIoWriterOutput = clazzLoader.loadClass(CLASS_COM_ALTOVA_IO_WRITEROUTPUT);

        consComAltovaIoReaderInput = classComAltovaIoReaderInput.getConstructor(java.io.Reader.class);
        consComAltovaIoWriterOutput = classComAltovaIoWriterOutput.getConstructor(java.io.Writer.class);

        mapforceObject = classMapforceGenerated.newInstance();
        mapforceMethod = classMapforceGenerated.getMethod(METHOD_RUN, new Class[]{classComAltovaIoInput, classComAltovaIoOutput});


    }

    public void downloadJarFromBlob(String jarBlobPath) {

        logger.info("Attempting to download jar from container=" + blobContainerName + ", path="  + jarBlobPath + ", using connstr=" + blobConnString);

        // Create a BlobServiceClient object which will be used to create a container client
        BlobServiceClient blobServiceClient =
                new BlobServiceClientBuilder().connectionString(blobConnString).buildClient();

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(blobContainerName);

        BlobClient blobClient = containerClient.getBlobClient(jarBlobPath);
        blobClient.downloadToFile(mapforceJarLocalTempFilePath,true);

        logger.info("JAR downloaded to " +mapforceJarLocalTempFilePath);

    }


}
