package com.gitblit.plugin.pycheck;

import java.util.HashMap;
import java.util.Map;

public class ThreadAttributes {

  private static final ThreadLocal<Map<String, String>> THREAD_ATTRIBUTES =
      new ThreadLocal<Map<String, String>>() {
        @Override
        protected Map<String, String> initialValue() {
          return new HashMap<>();
        }
      };

  public static String get(String key) {
    return THREAD_ATTRIBUTES.get().get(key);
  }

  public static void set(String key, String value) {
    THREAD_ATTRIBUTES.get().put(key, value);
  }
}
