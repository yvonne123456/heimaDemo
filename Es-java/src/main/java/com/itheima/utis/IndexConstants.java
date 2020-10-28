package com.itheima.utis;

public abstract class IndexConstants {

    public static final String INDEX_SETTINGS = "{\n" +
            "  \"settings\": {\n" +
            "    \"number_of_shards\": 3, \n" +
            "    \"number_of_replicas\": 1\n" +
            "  },\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"age\": {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"gender\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"note\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\",\n" +
            "        \"search_analyzer\": \"ik_smart\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
