package com.group15.b07project;

public class DocMetadataStructure {
    String docId;
    DocsdataStructure docsdata;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public DocsdataStructure getDocsdata() {
        return docsdata;
    }

    public void setDocsdata(DocsdataStructure docsdata) {
        this.docsdata = docsdata;
    }

    public DocMetadataStructure(String docId, DocsdataStructure docsdata) {
        this.docId = docId;
        this.docsdata = docsdata;
    }
}
