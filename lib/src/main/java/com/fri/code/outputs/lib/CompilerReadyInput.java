package com.fri.code.outputs.lib;

public class CompilerReadyInput{
    private String clientId;
    private String clientSecret;
    private String script;
    private String stdin;
    private String language;
    private String versionIndex;


    public CompilerReadyInput() {
        this.clientId = "336e764a0d15862c64c12304e1d90687";
        this.clientSecret = "a886859dc6d68b2744c1b434ad7c4ceb3611f5877a905a05e5f7375665f40a73";
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getStdin() {
        return stdin;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    public String getVersionIndex() {
        return versionIndex;
    }

    public void setVersionIndex(String versionIndex) {
        this.versionIndex = versionIndex;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
