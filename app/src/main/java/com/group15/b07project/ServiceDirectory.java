package com.group15.b07project;

import java.util.HashMap;
import java.util.List;

/**
 * Holds a mapping from city names to lists of ServiceEntry objects.
 * Gson will parse the JSON into this structure.
 */
public class ServiceDirectory {
    // The variable name 'cities' should match the JSON object's property if using a wrapper.
    // Since our JSON directly has city keys at the root, we'll parse JSON into HashMap directly in code.
    public HashMap<String, List<ServiceEntry>> cities;
}