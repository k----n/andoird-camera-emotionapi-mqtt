package info.staticfree.mqtt_camera.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONObject;

import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;

import info.staticfree.mqtt_camera.mqtt.MqttRemote;

/**
 * Publishes the image to an MQTT subTopic.
 */
public class ImagePublisher implements Runnable {
    private final Image image;
    private final String subTopic;
    private final MqttRemote mqttRemote;

    public ImagePublisher(@NonNull Image image, @NonNull MqttRemote mqttRemote,
            @NonNull String subTopic) {
        this.image = image;
        this.mqttRemote = mqttRemote;
        this.subTopic = subTopic;
    }

    @Override
    public void run() {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        //String img = getBase64Image(imageBitmap);
        //logLargeString(img);
        String emotion = getEmotions(getBase64Binary(imageBitmap));
        try {
            mqttRemote.publish(subTopic, emotion.getBytes());
        } finally {
            image.close();
        }
    }

    private void logLargeString(String content) {
        if (content.length() > 3000) {
            Log.d("mi", content.substring(0, 3000));
            logLargeString(content.substring(3000));
        } else {
            Log.d("mi", content);
        }
    }

    private String getBase64Image(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private byte[] getBase64Binary(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return bytes;
    }

    private String getEmotions(byte[] binary){
        HttpClient httpClient = new DefaultHttpClient();

        try
        {
            // NOTE: You must use the same region in your REST call as you used to obtain your subscription keys.
            //   For example, if you obtained your subscription keys from westcentralus, replace "westus" in the
            //   URL below with "westcentralus".
            URIBuilder uriBuilder = new URIBuilder("https://westus.api.cognitive.microsoft.com/emotion/v1.0/recognize");

            URI uri = uriBuilder.build();
            HttpPost request = new HttpPost(uri);

            // Request headers. Replace the example key below with your valid subscription key.
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", "NULL");

            // Request body. Replace the example URL below with the URL of the image you want to analyze.
            ByteArrayEntity reqEntity = new ByteArrayEntity(binary);
            request.setEntity(reqEntity);

            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                String retSrc = EntityUtils.toString(entity);
                Log.d("RESULT", retSrc);
                return retSrc;
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return "";
    }
}

