package cn.kevinwang.mybatis.boot.spring;

import org.springframework.asm.ClassReader;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author wang
 * @create 2024-01-19-16:51
 */
public class SimpleMetadataReader implements MetadataReader {
    private final Resource resource;

    private final ClassMetadata classMetadata;

    private final AnnotationMetadata annotationMetadata;

    public SimpleMetadataReader(Resource resource, ClassLoader classLoader) throws IOException {
        ClassReader classReader;
        try (InputStream is = new BufferedInputStream(resource.getInputStream())) {
            classReader = new ClassReader(is);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("ASM ClassReader failed to parse class file - " +
                    "probably due to a new Java class file version that isn't supported yet: " + resource, ex);
        }

        AnnotationMetadata visitor = AnnotationMetadata.introspect(SimpleMetadataReader.class);

        this.annotationMetadata = visitor;
        // (since AnnotationMetadataReadingVisitor extends ClassMetadataReadingVisitor)
        this.classMetadata = visitor;
        this.resource = resource;
    }
    @Override
    public Resource getResource() {
        return null;
    }

    @Override
    public ClassMetadata getClassMetadata() {
        return null;
    }

    @Override
    public AnnotationMetadata getAnnotationMetadata() {
        return null;
    }
}
