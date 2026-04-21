package com.ai.baemin.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class RagDocumentLoaderTest {

    @TestConfiguration
    static class FakeEmbeddingConfig {
        @Bean
        @Primary
        EmbeddingModel fakeEmbeddingModel() {
            EmbeddingModel fake = mock(EmbeddingModel.class);
            float[] dummy = new float[]{0.1f, 0.2f, 0.3f};
            when(fake.embed(any(Document.class))).thenReturn(dummy);
            when(fake.embed(any(String.class))).thenReturn(dummy);
            return fake;
        }
    }

    @Autowired
    SimpleVectorStore vectorStore;

    @Test
    void 앱_시작시_FAQ_문서가_벡터스토어에_로드된다() {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query("배달 시간").topK(3).build()
        );

        assertThat(results).isNotEmpty();
    }

    @Test
    void 환불정책_키워드로_관련_문서를_검색할_수_있다() {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query("환불 처리 기간").topK(3).build()
        );

        assertThat(results).isNotEmpty();
    }
}
