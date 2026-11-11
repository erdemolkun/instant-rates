package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.ParaGarantiRate
import dynoapps.exchange_rates.util.L
import okhttp3.ResponseBody
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.Text
import org.xml.sax.SAXException
import retrofit2.Converter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/** Parses the `<STOCK><SYMBOL/><LAST/></STOCK>` XML feed. */
object ParagarantiConverter : Converter<ResponseBody, List<BaseRate>> {

    override fun convert(value: ResponseBody): List<BaseRate> {
        val rates = ArrayList<BaseRate>()
        try {
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(value.string().byteInputStream(Charsets.UTF_8))
            for (stock in document.childNodes.item(1).childNodes.elements()) {
                if (stock.tagName != "STOCK") continue
                val rate = ParaGarantiRate()
                for (child in stock.childNodes.elements()) {
                    when (child.tagName) {
                        "SYMBOL" -> rate.symbol = (child.childNodes.item(0) as Text).wholeText
                        "LAST" -> rate.last = (child.childNodes.item(0) as Text).wholeText
                    }
                }
                rates.add(rate)
            }
        } catch (e: SAXException) {
            L.ex(e, "Paragaranti XML parse failed")
        } catch (e: ParserConfigurationException) {
            L.ex(e, "Paragaranti XML parse failed")
        }
        return rates
    }

    private fun NodeList.elements(): List<Element> =
        (0 until length).map { item(it) }.filter { it.nodeType == Node.ELEMENT_NODE }.map { it as Element }
}
