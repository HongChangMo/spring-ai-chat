package com.ai.baemin.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class VectorStoreConfig {

    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public ApplicationRunner documentLoader(SimpleVectorStore vectorStore) {
        return args -> {
            List<Document> documents = new ArrayList<>();
            for (String path : List.of("docs/faq.txt", "docs/refund-policy.txt")) {
                TextReader reader = new TextReader(new ClassPathResource(path));
                documents.addAll(reader.read());
            }
            vectorStore.add(documents);
        };
    }
}
