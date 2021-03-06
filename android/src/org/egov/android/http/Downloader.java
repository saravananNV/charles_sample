/**
 * eGov suite of products aim to improve the internal efficiency,transparency, accountability and the service delivery of the
 * government organizations.
 * 
 * Copyright (C) <2015> eGovernments Foundation
 * 
 * The updated version of eGov suite of products as by eGovernments Foundation is available at http://www.egovernments.org
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/ or http://www.gnu.org/licenses/gpl.html .
 * 
 * In addition to the terms of the GPL license to be adhered to in using this program, the following additional terms are to be
 * complied with:
 * 
 * 1) All versions of this program, verbatim or modified must carry this Legal Notice.
 * 
 * 2) Any misrepresentation of the origin of the material is prohibited. It is required that all modified versions of this
 * material be marked in reasonable ways as different from the original version.
 * 
 * 3) This license does not grant any rights to any user of the program with regards to rights under trademark law for use of the
 * trade names or trademarks of eGovernments Foundation.
 * 
 * In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.android.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.os.AsyncTask;
import android.util.Log;

public class Downloader extends AsyncTask<Void, Integer, byte[]> {

    private static final String TAG = Downloader.class.getName();

    private IHttpClientListener listener;
    private String requestMethod = "GET";
    private String destination = "";
    private Map<String, Object> params;
    private HttpURLConnection con = null;
    private String url = null;

    private boolean hasError = false;

    public Downloader() {
        params = new HashMap<String, Object>();
    }

    //----------------------------------------------------------------------------//

  
    //----------------------------------------------------------------------------//

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Downloader addParams(String key, String value) {
        params.put(key, value);
        return this;
    }

    //----------------------------------------------------------------------------//

    public IHttpClientListener getListener() {
        return listener;
    }

    public Downloader setListener(IHttpClientListener listener) {
        this.listener = listener;
        return this;
    }

    //----------------------------------------------------------------------------//

    public String getDestination() {
        return destination;
    }

    public Downloader setDestination(String path) {
        this.destination = path;
        return this;
    }

    //----------------------------------------------------------------------------//

    public String getRequestMethod() {
        return requestMethod;
    }

    public Downloader setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    //----------------------------------------------------------------------------//

    public void start() {
        execute();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    //----------------------------------------------------------------------------//

    @Override
    protected void onPostExecute(byte[] result) {
        super.onPostExecute(result);
        if (hasError) {
            listener.onError(result);
        } else {
            listener.onComplete(result);
        }
    }

    //----------------------------------------------------------------------------//

    @Override
    protected byte[] doInBackground(Void... params) {
        try {

            String url = this.url;

            if (!this.params.isEmpty()) {
                String param = "";
                Set<Entry<String, Object>> paramsSet = this.params.entrySet();
                for (Entry<String, Object> obj : paramsSet) {
                    String p = "";
                    try {
						p = obj.getKey()
								+ "="
								+ URLEncoder
										.encode(String.valueOf(obj.getValue()),
												"UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    param += (param.length() == 0) ? p : "&" + p;
                }
                if (!param.isEmpty()) {
                    url = url + "?" + param;
                }
            }

            Log.d(TAG, "Download Start => " + url);

            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod(requestMethod);
            con.setUseCaches(false);
            con.setDoInput(true);
            //con.setReadTimeout(10000);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            InputStream input = con.getInputStream();
            int length = con.getContentLength();

            byte[] data = new byte[1024];
            int count = 0, total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                if (length > 0) {
                    publishProgress((int) (total * 100 / length));
                }
                out.write(data, 0, count);
            }
            input.close();

            out.flush();
            if (destination != null && !destination.equalsIgnoreCase("") && out.size() > 100) {
                String folder = this.destination.substring(0, this.destination.lastIndexOf("/"));
                File f = new File(folder);
                f.mkdirs();
                FileOutputStream fs = new FileOutputStream(new File(this.destination));
                out.writeTo(fs);
                fs.close();
            }
            hasError = con.getResponseCode() != 200;
            //content = out.toByteArray();
            out.close();

        } catch (Exception e) {
            hasError = true;
            e.printStackTrace();
        } finally {
            con.disconnect();
        }

        return null;
    }

    //----------------------------------------------------------------------------//

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        listener.onProgress(values[0]);
    }
    //----------------------------------------------------------------------------//

}
