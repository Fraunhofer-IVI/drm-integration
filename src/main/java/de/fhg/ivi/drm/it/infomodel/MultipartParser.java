/*
  Copyright 2022 Fraunhofer Institute for Transportation and Infrastructure Systems IVI

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.


  This file has been copied from
  https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/blob/3.0.0/messaging/src/main/java/de/fraunhofer/ids/messaging/protocol/multipart/parser/MultipartParser.java

  Adaptions have been made to fit to the file to this project.
*/

package de.fhg.ivi.drm.it.infomodel;

import lombok.Getter;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility Class for parsing Multipart Maps from String responses.
 */
public class MultipartParser implements UploadContext {
    private final String postBody;
    private final String boundary;

    @Getter
    private final Map<String, String> parameters = new ConcurrentHashMap<>();

    /**
     * Constructor for the MultipartStringParser used internally to parse a multipart response to a Map<Partname, MessagePart>.
     *
     * @param postBody a multipart response body as string
     * @throws FileUploadException if there are problems reading/parsing the postBody.
     */
    private MultipartParser(final String postBody) throws FileUploadException, IOException {
        this.postBody = postBody;

        if (postBody.length() <= 2 || postBody.indexOf('\n') <= 2)  {
            throw new IOException("String could not be parsed, could not find a boundary!");
        }

        this.boundary = postBody.substring(2, postBody.indexOf('\n')).trim();

        final var upload = new FileUpload(new DiskFileItemFactory());
        final var fileItems = upload.parseRequest(this);

        for (final var fileItem : fileItems) {
            if (fileItem.isFormField()) {
                //put the parameters into the map as "name, content"
                parameters.put(fileItem.getFieldName(), fileItem.getString());
            }
        }
    }

    /**
     * Convert a String from a multipart response to a Map with Partname/MessagePart.
     *
     * @param postBody a multipart response body as string
     * @return a Map from partname on content
     * @throws IOException if there are problems reading/parsing the postBody.
     */
    public static Map<String, String> stringToMultipart(final String postBody) throws IOException {
        try {
            final var resultMap = new MultipartParser(postBody).getParameters();

            if (resultMap.keySet().isEmpty()) {
                throw new IOException("Could not parse Multipart! No parts found!");
            }

            return resultMap;
        } catch (FileUploadException e) {
            throw new IOException("Could not parse given String:\n" + postBody, e);
        }
    }

    //these methods must be implemented because of the UploadContext interface
    @Override
    public long contentLength() {
        return postBody.length();
    }

    @Override
    public String getCharacterEncoding() {
        return "Cp1252";
    }

    @Override
    public String getContentType() {
        return "multipart/form-data, boundary=" + this.boundary;
    }

    @Override
    public int getContentLength() {
        return -1;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(postBody.getBytes());
    }
}
