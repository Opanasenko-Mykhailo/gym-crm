package com.gcm.testutils;

import io.cucumber.datatable.DataTable;

import java.util.HashMap;
import java.util.Map;

public class DataTableUtils {

    private DataTableUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Map<String, String> extractData(DataTable dataTable) {
        if (dataTable.height() > 1 && dataTable.width() == 2) {
            return dataTable.asMap(String.class, String.class);
        }
        return dataTable.asMaps().get(0);
    }

    public static Map<String, String> normalizeEmptyValues(Map<String, String> data) {
        Map<String, String> normalized = new HashMap<>(data);
        normalized.replaceAll((k, v) -> v == null ? "" : v);

        return normalized;
    }
}