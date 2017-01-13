package dynoapps.exchange_rates.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.List;

import dynoapps.exchange_rates.model.rates.ParaGarantiRate;

/**
 * Created by erdemmac on 13/01/2017.
 */

@Root(name = "ICPIYASA", strict = false)
public class ParagarantiResponse {

    @ElementListUnion({
            @ElementList(entry="STOCK", inline=true),
            @ElementList(entry="PARITY", inline=true),
    })
    public List<ParaGarantiRate> rates;
}
