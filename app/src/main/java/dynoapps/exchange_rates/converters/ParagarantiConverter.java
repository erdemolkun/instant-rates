package dynoapps.exchange_rates.converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.ParaGarantiRate;
import dynoapps.exchange_rates.util.L;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class ParagarantiConverter implements Converter<ResponseBody, List<BaseRate>> {

    private static final ParagarantiConverter INSTANCE = new ParagarantiConverter();

    private ParagarantiConverter() {
    }

    /**
     * Sample response body
     * </p>
     **/
    @Override
    public List<BaseRate> convert(@NonNull ResponseBody value) throws IOException {

        ArrayList<BaseRate> rates = new ArrayList<>();


        String responseBody = value.string();
        InputStream inputStream = new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8));
        try {
            Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            NodeList nodeList = d.getChildNodes().item(1).getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    if (element.getTagName().equals("STOCK")) {
                        NodeList stockChildNodes = node.getChildNodes();
                        ParaGarantiRate rate = new ParaGarantiRate();
                        for (int j = 0; j < stockChildNodes.getLength(); j++) {
                            Node nodeChildStock = stockChildNodes.item(j);
                            if (nodeChildStock instanceof Element) {
                                Element elementChild = (Element) nodeChildStock;
                                if (elementChild.getTagName().equals("SYMBOL")) {
                                    rate.symbol = ((Text) (elementChild.getChildNodes().item(0))).getWholeText();
                                } else if (elementChild.getTagName().equals("LAST")) {
                                    rate.last = ((Text) (elementChild.getChildNodes().item(0))).getWholeText();
                                }
                            }

                        }
                        rates.add(rate);
                    }
                }
            }

        } catch (SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        return rates;
    }

    /**
     * Factory for creating converter. We only care about decoding responses.
     **/
    public static final class Factory extends Converter.Factory {

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type,
                                                                Annotation[] annotations,
                                                                Retrofit retrofit) {
            return INSTANCE;
        }

    }


}