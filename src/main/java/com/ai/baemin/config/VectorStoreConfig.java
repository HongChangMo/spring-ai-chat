package com.ai.baemin.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class VectorStoreConfig {

    @Value("${rag.document-paths}")
    private List<String> documentPaths;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public ApplicationRunner documentLoader(VectorStore vectorStore) {
        return args -> {
            List<Document> documents = new ArrayList<>();
            for (String path : documentPaths) {
                TextReader reader = new TextReader(new ClassPathResource(path));
                documents.addAll(reader.read());
            }
            vectorStore.add(documents);
        };
    }
}
