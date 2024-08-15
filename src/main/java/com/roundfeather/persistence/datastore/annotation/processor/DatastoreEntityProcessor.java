package com.roundfeather.persistence.datastore.annotation.processor;

import com.google.auto.service.AutoService;
import com.roundfeather.persistence.datastore.annotation.DatastoreEntity;
import com.roundfeather.persistence.datastore.annotation.DatastoreKey;
import com.roundfeather.persistence.datastore.DatastoreRepository;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

/**
 * {@link AbstractProcessor} for processing the {@link DatastoreEntity} annotation. Handles compile time validations
 * for the classes with this annotation.
 *
 * <p>
 *     Generates an implementation of the {@link DatastoreRepository} interface for the annotated classes
 * </p>
 * <p>
 *     Checks and provides a warning if a class will be serializable
 * </p>
 *
 * @since 1.0
 */
@SupportedAnnotationTypes({"com.roundfeather.persistence.datastore.annotation.DatastoreEntity"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class DatastoreEntityProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            generateRepository(annotation, roundEnv);
            verifySerializable(annotation, roundEnv);
        }

        return true;
    }

    /**
     * Verifies if the class annotated with {@link DatastoreEntity} contains at least 1 field annotated with {@link DatastoreKey}
     * and provides a compile time warning if it doesn't
     *
     * @param annotation the annotation interface being processed
     * @param roundEnv environment for information about the current and prior round
     *
     * @since 1.0
     */
    private void verifySerializable(TypeElement annotation, RoundEnvironment roundEnv) {
        Set<? extends Element> classes = roundEnv.getElementsAnnotatedWith(annotation);
        List<String> classesWithKey = roundEnv.getElementsAnnotatedWith(DatastoreKey.class).stream()
                .map(Element::getEnclosingElement)
                .map(ee -> ((TypeElement) ee).getQualifiedName().toString())
                .toList();

        classes.forEach(
                e -> {
                    TypeElement classElement = (TypeElement) e;
                    if (!classesWithKey.contains(classElement.getQualifiedName().toString())) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, String.format("Class '%s' missing DatastoreKey and can't be serialized", classElement.getQualifiedName().toString()), e);
                    }
                }
        );
    }

    /**
     * Generates a {@link DatastoreRepository} implementation for the classes annotated with {@link DatastoreEntity}.
     * If {@link DatastoreEntity#autoGenerateRepository()} is {@code false} then it will skip the repository
     *
     * @param annotation the annotation interface being processed
     * @param roundEnv environment for information about the current and prior round
     *
     * @since 1.0
     */
    private void generateRepository(TypeElement annotation, RoundEnvironment roundEnv) {
        Set<? extends Element> classes = roundEnv.getElementsAnnotatedWith(annotation);
        classes.forEach(
                e -> {
                    DatastoreEntity deAnnotation = e.getAnnotation(DatastoreEntity.class);
                    if (deAnnotation.autoGenerateRepository()) {

                        TypeElement clazz = (TypeElement) e;
                        String qualifiedName = clazz.getQualifiedName().toString();

                        int lastDot = qualifiedName.lastIndexOf('.');

                        String packageName = qualifiedName.substring(0, lastDot) + ".repository";
                        String entityClassName = clazz.getSimpleName().toString();
                        String className = clazz.getSimpleName().toString() + "Repository";

                        try {
                            JavaFileObject repoFile = processingEnv.getFiler().createSourceFile(packageName + "." + className);
                            PrintWriter writer = new PrintWriter(repoFile.openWriter());

                            writer.print("package ");
                            writer.print(packageName);
                            writer.println(";");
                            writer.println();

                            writer.print("import ");
                            writer.print(clazz.getQualifiedName().toString());
                            writer.println(";");
                            writer.println("import com.google.cloud.datastore.Datastore;");
                            writer.println("import jakarta.inject.Inject;");
                            writer.println("import jakarta.enterprise.context.ApplicationScoped;");
                            writer.println("import com.roundfeather.persistence.datastore.DatastoreRepository;");
                            writer.println();

                            writer.println("@ApplicationScoped");
                            writer.print("public class ");
                            writer.print(className);
                            writer.print(" implements DatastoreRepository<");
                            writer.print(entityClassName);
                            writer.println("> {");
                            writer.println();
                            writer.println("@Inject");
                            writer.println("Datastore datastore;");
                            writer.println();
                            writer.println("}");

                            writer.close();

                            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format("Generated Repository '%s.%s'", packageName, className));
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
        );
    }
}
