// Copyright 2019 Oath Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package ai.vespa.example.album;

import com.yahoo.application.ApplicationBuilder;
import com.yahoo.application.Networking;
import com.yahoo.application.container.DocumentProcessing;
import com.yahoo.application.container.JDisc;
import com.yahoo.component.ComponentId;
import com.yahoo.component.ComponentSpecification;
import com.yahoo.docproc.DocumentProcessor;
import com.yahoo.docproc.Processing;
import com.yahoo.document.Document;
import com.yahoo.document.DocumentPut;
import com.yahoo.document.DocumentType;
import com.yahoo.processing.execution.chain.ChainRegistry;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Rot13DocumentProcessorTest {

    @Test
    void requireThatBasicDocumentProcessingWorksForDoc() throws IOException {
        JDisc container = new ApplicationBuilder()
                .servicesXml("<container version=\"1.0\">\n" +
                        "  <document-processing>\n" +
                        "    <chain id=\"myChain\">\n" +
                        "      <documentprocessor id=\"" +
                        Rot13DocumentProcessor.class.getCanonicalName() + "\"/>\n" +
                        "    </chain>\n" +
                        "  </document-processing>\n" +
                        "</container>\n")
                .documentType("music",
                        "document music {\n" +
                                "  field album type string { }\n" +
                                "}\n")
                .networking(Networking.disable)
                .build()
                .getJDisc("jdisc");

        DocumentProcessing docProc = container.documentProcessing();
        DocumentType type = docProc.getDocumentTypes().get("music");

        ChainRegistry<DocumentProcessor> chains = docProc.getChains();
        assertTrue(chains.allComponentsById().containsKey(new ComponentId("myChain")));

        Document doc = new Document(type, "id:test:music::this:is:a:great:album");
        doc.setFieldValue("album", "Great Album!");

        Processing processing = new Processing();
        processing.addDocumentOperation(new DocumentPut(doc));
        DocumentProcessor.Progress progress = docProc.process(ComponentSpecification.fromString("myChain"), processing);

        assertSame(DocumentProcessor.Progress.DONE, progress);
        assertEquals("Terng Nyohz!", doc.getFieldValue("album").toString());

        container.close();
    }
}
