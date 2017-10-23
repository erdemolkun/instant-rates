package dynoapps.exchange_rates.data;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import dynoapps.exchange_rates.event.RatesEvent;
import dynoapps.exchange_rates.model.rates.BaseRate;

/**
 * Copyright 2016 Erdem OLKUN
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

public class RatesHolder {

    private static final int MAX_RATES_COUNT = 10;

    private static RatesHolder instance;
    private SparseArray<List<RatesEvent>> rateEventsSparse;

    public static RatesHolder getInstance() {
        if (instance == null)
            instance = new RatesHolder();
        return instance;
    }

    public RatesEvent getLatestEvent(int source_type) {
        if (rateEventsSparse != null && rateEventsSparse.get(source_type, null) != null) {
            List<RatesEvent> events = rateEventsSparse.get(source_type);
            return events.get(events.size() - 1);
        }
        return null;
    }

    public <T extends BaseRate> void addRate(List<T> rates, long fetchTime, int type) {
        if (rateEventsSparse == null) {
            rateEventsSparse = new SparseArray<>();
        }
        RatesEvent ratesEvent = new RatesEvent<>(rates, type, fetchTime);
        List<RatesEvent> ratesEvents = rateEventsSparse.get(type);
        if (ratesEvents == null) ratesEvents = new ArrayList<>();
        ratesEvents.add(ratesEvent);
        if (ratesEvents.size() > MAX_RATES_COUNT) {
            ratesEvents.remove(0);
        }
        rateEventsSparse.put(type, ratesEvents);
    }

    public <T extends BaseRate> void addRate(List<T> rates, int type) {
        if (rateEventsSparse == null) {
            rateEventsSparse = new SparseArray<>();
        }
        RatesEvent ratesEvent = new RatesEvent<>(rates, type, System.currentTimeMillis());
        List<RatesEvent> ratesEvents = rateEventsSparse.get(type);
        if (ratesEvents == null) ratesEvents = new ArrayList<>();
        ratesEvents.add(ratesEvent);
        rateEventsSparse.put(type, ratesEvents);
    }
}
