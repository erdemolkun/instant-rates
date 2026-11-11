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

    private static volatile RatesHolder instance;
    private final SparseArray<List<RatesEvent>> rateEventsSparse = new SparseArray<>();

    public static RatesHolder getInstance() {
        if (instance == null) {
            synchronized (RatesHolder.class) {
                if (instance == null) {
                    instance = new RatesHolder();
                }
            }
        }
        return instance;
    }

    public synchronized RatesEvent getLatestEvent(int source_type) {
        List<RatesEvent> events = rateEventsSparse.get(source_type);
        if (events == null || events.isEmpty()) return null;
        return events.get(events.size() - 1);
    }

    public <T extends BaseRate> void addRate(List<T> rates, int type) {
        addRate(rates, System.currentTimeMillis(), type);
    }

    /**
     * Stores a fetch result, keeping only the latest {@link #MAX_RATES_COUNT} events per source so
     * a long-running polling service does not grow without bound.
     */
    public synchronized <T extends BaseRate> void addRate(List<T> rates, long fetchTime, int type) {
        List<RatesEvent> ratesEvents = rateEventsSparse.get(type);
        if (ratesEvents == null) {
            ratesEvents = new ArrayList<>();
            rateEventsSparse.put(type, ratesEvents);
        }
        ratesEvents.add(new RatesEvent<>(rates, type, fetchTime));
        while (ratesEvents.size() > MAX_RATES_COUNT) {
            ratesEvents.remove(0);
        }
    }
}
