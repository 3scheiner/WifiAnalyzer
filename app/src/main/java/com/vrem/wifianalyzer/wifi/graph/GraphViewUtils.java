/*
 *    Copyright (C) 2015 - 2016 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.vrem.wifianalyzer.wifi.graph;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.wifi.AccessPointsDetail;
import com.vrem.wifianalyzer.wifi.model.WiFiBand;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GraphViewUtils {
    private static final float TEXT_SIZE_ADJUSTMENT = 0.75f;
    private final MainContext mainContext = MainContext.INSTANCE;
    private final GraphView graphView;
    private final Map<WiFiDetail, ? extends Series<DataPoint>> seriesMap;
    private GraphLegend graphLegend;

    public GraphViewUtils(@NonNull GraphView graphView, @NonNull Map<WiFiDetail, ? extends Series<DataPoint>> seriesMap,
                          @NonNull GraphLegend graphLegend) {
        this.graphView = graphView;
        this.seriesMap = seriesMap;
        this.graphLegend = graphLegend;
    }

    void updateSeries(@NonNull Set<WiFiDetail> newSeries) {
        List<WiFiDetail> remove = new ArrayList<>();
        for (WiFiDetail wiFiDetail : seriesMap.keySet()) {
            if (!newSeries.contains(wiFiDetail)) {
                graphView.removeSeries(seriesMap.get(wiFiDetail));
                remove.add(wiFiDetail);
            }
        }
        for (WiFiDetail wiFiDetail : remove) {
            seriesMap.remove(wiFiDetail);
        }
    }

    void updateLegend(@NonNull GraphLegend graphLegend) {
        resetLegendRenderer(graphLegend);
        LegendRenderer legendRenderer = graphView.getLegendRenderer();
        legendRenderer.resetStyles();
        legendRenderer.setWidth(0);
        legendRenderer.setTextSize(legendRenderer.getTextSize() * TEXT_SIZE_ADJUSTMENT);
        graphLegend.display(legendRenderer);
    }

    private void resetLegendRenderer(@NonNull GraphLegend graphLegend) {
        if (!this.graphLegend.equals(graphLegend)) {
            LegendRenderer legendRenderer = new LegendRenderer(graphView);
            graphView.setLegendRenderer(legendRenderer);
            this.graphLegend = graphLegend;
        }
    }

    void setVisibility(@NonNull WiFiBand wiFiBand) {
        graphView.setVisibility(wiFiBand.equals(mainContext.getSettings().getWiFiBand()) ? View.VISIBLE : View.GONE);
    }

    public void setOnDataPointTapListener(Series<DataPoint> series) {
        series.setOnDataPointTapListener(new GraphTapListener());
    }

    private class GraphTapListener implements OnDataPointTapListener {
        @Override
        public void onTap(@NonNull Series series, @NonNull DataPointInterface dataPoint) {
            for (WiFiDetail wiFiDetail : seriesMap.keySet()) {
                Series<DataPoint> channelGraphSeries = seriesMap.get(wiFiDetail);
                if (series == channelGraphSeries) {
                    Dialog dialog = new AccessPointsDetail().popupDialog(mainContext.getContext(), mainContext.getLayoutInflater(), wiFiDetail);
                    dialog.show();
                    return;
                }
            }
        }
    }
}
