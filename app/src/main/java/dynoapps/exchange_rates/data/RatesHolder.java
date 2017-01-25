package dynoapps.exchange_rates.data;

import android.util.SparseArray;

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

public class RatesHolder<T extends BaseRate> {

    private static RatesHolder instance;

    public static RatesHolder getInstance() {
        if (instance == null)
            instance = new RatesHolder();
        return instance;
    }

    private SparseArray<RatesEvent<T>> ratesArray;

    public RatesEvent<T> getRates(int source_type) {
        if (ratesArray != null && ratesArray.get(source_type, null) != null) {
            return ratesArray.get(source_type);
        }
        return null;
    }

    public SparseArray<RatesEvent<T>> getAllRates() {
        return ratesArray;
    }

    public void addRate(List<T> rates, long fetchMilis, int type) {
        if (ratesArray == null) {
            ratesArray = new SparseArray<>();
        }

        ratesArray.put(type, new RatesEvent<>(rates, type, fetchMilis));
    }

    public void addRate(List<T> rates, int type) {
        if (ratesArray == null) {
            ratesArray = new SparseArray<>();
        }

        ratesArray.put(type, new RatesEvent<>(rates, type, System.currentTimeMillis()));
    }
}
