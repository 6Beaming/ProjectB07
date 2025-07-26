package com.group15.b07project;

import java.util.List;

public class Question {
    public String id, type, text;
    public List<String> options;
    public String followupTextPrompt;  // for single+text
    public Object tips;//tips is either String or List<String>
}
