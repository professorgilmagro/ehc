package aiec.br.ehc.helper;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import aiec.br.ehc.R;
import aiec.br.ehc.adapter.ResourceViewHolder;

/**
 * Helper para parsear o retorno de requisição do servidor e retornar a informação configurada
 * no recurso de relação
 */

public class ResourceHolderParserHelper {
    private static final String DEFAULT_INFO = "N/D";
    private final ResourceViewHolder holder;
    private String httpResponse;

    public ResourceHolderParserHelper(String httpResponse, ResourceViewHolder resourceHolder) {
        this.httpResponse = httpResponse;
        this.holder = resourceHolder;
    }

    /**
     * Exibe a informação na view de relação com este item
     */
    public void showInfo() {
        String infoText = null;
        switch(holder.resource.getReadFormat()) {
            case "json":
                infoText = this.parseFromJson();
                break;

            case "html":
                infoText = this.fromHtml(this.parseFromHtml()).toString();
                break;

            case "txt":
                infoText = this.parseFromText();
                break;

            case "xml":
                infoText = this.parseFromXML();
                break;
        }

        if (!TextUtils.isEmpty(infoText)) {
            holder.info.setText(infoText);
            holder.info.setVisibility(View.VISIBLE);
            int textSize = infoText.length() > 3 ? 14 : 20;
            holder.info.setTextSize(textSize);
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    /**
     * Retorna o texto de resposta
     * @return string
     */
    public String parseFromText() {
        if (TextUtils.isEmpty(httpResponse)) {
            return DEFAULT_INFO;
        }

        return httpResponse.trim();
    }

    /**
     * Retorna o conteúdo html de resposta
     * @return string
     */
    public String parseFromHtml() {
        if (TextUtils.isEmpty(httpResponse)) {
            return DEFAULT_INFO;
        }

        return httpResponse;
    }

    /**
     * Faz o parse para o JSON de resposta
     * @return string
     */
    public String parseFromJson() {
        String infoText = DEFAULT_INFO;
        try {
            JSONObject object = new JSONObject(httpResponse);
            String[] nodes = holder.resource.getReadNode().trim().replace(" ", "").split("\\.");

            String lastNode = nodes[nodes.length - 1];
            for (String node : nodes) {
                Iterator<String> keys = object.keys();
                while (keys.hasNext() && !node.equals(lastNode)) {
                    String key = keys.next();
                    if (key.equalsIgnoreCase(node.trim())) {
                        object = object.getJSONObject(key);
                        break;
                    }
                }
            }

            infoText = object.getString(lastNode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return infoText;
    }

    /**
     * Retorna a informação do node configurado no recurso dentro do XML de respota
     * @return string
     */
    public String parseFromXML() {
        String infoText = DEFAULT_INFO;
        XmlPullParserFactory xmlFactory = null;
        try {
            xmlFactory = XmlPullParserFactory.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            InputStream is = new ByteArrayInputStream(httpResponse.getBytes());
            XmlPullParser parser = xmlFactory.newPullParser();
            parser.setInput(is, null);
            String node = holder.resource.getReadNode().trim();
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT)  {
                String name = parser.getName();
                switch (event){
                    case XmlPullParser.START_TAG:
                        if(name.equalsIgnoreCase(node)){
                            infoText = parser.getAttributeValue(null, "value");
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (TextUtils.isEmpty(infoText)) {
                            infoText = parser.getText();
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        break;
                }

                try {
                    event = parser.next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        return infoText;
    }
}
