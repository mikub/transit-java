// Copyright (c) Cognitect, Inc.
// All rights reserved.

package com.cognitect.transit.impl;

import java.util.HashMap;
import java.util.Map;

public class WriteCache {

    public static final int MIN_SIZE_CACHEABLE = 4;
    public static final int CACHE_CODE_DIGITS = 44;
    public static final int MAX_CACHE_ENTRIES = CACHE_CODE_DIGITS * CACHE_CODE_DIGITS;
    public static final int BASE_CHAR_IDX = 48;

    private final Map<String, String> cache;
    private int index;
    private boolean enabled;

    public WriteCache() { this(true); }

    public WriteCache(boolean enabled) {
        index = 0;
        cache = new HashMap<String, String>(MAX_CACHE_ENTRIES);
        this.enabled = enabled;
    }

    public static boolean isCacheable(String s, boolean asMapKey) {
        return (s.length() >= MIN_SIZE_CACHEABLE) &&
                 (asMapKey ||
                    (s.charAt(0) == Constants.ESC &&
                    (s.charAt(1) == ':' || s.charAt(1) == '$' || s.charAt(1) == '#')));
    }

    private String indexToCode(int index) {
        int hi = index / CACHE_CODE_DIGITS;
        int lo = index % CACHE_CODE_DIGITS;
        if (hi == 0) {
            return Constants.SUB_STR + (char)(lo + BASE_CHAR_IDX);
        } else {
            return Constants.SUB_STR + (char)(hi + BASE_CHAR_IDX) + (char)(lo + BASE_CHAR_IDX);
        }
    }

    public String cacheWrite(String s, boolean asMapKey) {

        if(enabled && isCacheable(s, asMapKey)) {
            String val = cache.get(s);
            if(val != null)
                return val;
            else {
                if(index == MAX_CACHE_ENTRIES)
                    init();
                String code = indexToCode(index++);
                cache.put(s, code);
            }
        }
        return s;
    }

	public WriteCache init(){
		index = 0;
		cache.clear();
		return this;
	}
}
