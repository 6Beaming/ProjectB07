package com.group15.b07project;

public class DocMetadataStructure {
    String docId;
    DocsDataStructure docsData;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public DocsDataStructure getDocsData() {
        return docsData;
    }

    public void setDocsData(DocsDataStructure docsData) {
        this.docsData = docsData;
    }

    public DocMetadataStructure(String docId, DocsDataStructure docsData) {
        this.docId = docId;
        this.docsData = docsData;
    }
}
