package com.roundfeather.persistence.utils.datastore.annotation.processor;

import com.google.auto.service.AutoService;
import com.roundfeather.persistence.utils.datastore.annotation.DatastoreKey;
import com.roundfeather.persistence.utils.datastore.annotation.KeyType;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link AbstractProcessor} for processing the {@link DatastoreKey} annotation. Handles compile time validations
 * for fields with this annotation.
 *
 * <p>
 *     Validates that classes with fields annotated with {@link DatastoreKey} have at most 1 annotated field
 * </p>
 * <p>
 *     Validates that the {@link KeyType} of the {@link DatastoreKey} matches the datatype of the annotated field
 * </p>
 *
 * @since 1.0
 */
@SupportedAnnotationTypes({"com.roundfeather.persistence.utils.datastore.annotation.DatastoreKey"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class DatastoreKeyProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            validateKeyType(annotation, roundEnv);
            validateKeyNumber(annotation, roundEnv);
        }

        return true;
    }

    /**
     * Validates that the fields annotated with {@link DatastoreKey} have a {@link KeyType} that matches the type of the field
     *
     * @param annotation the annotation interface being processed
     * @param roundEnv environment for information about the current and prior round
     *
     * @since 1.0
     */
    private void validateKeyType(TypeElement annotation, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
        Set<VariableElement> fields = ElementFilter.fieldsIn(elements);

        List<String> messages = fields.stream()
                .map(ve -> {
                    DatastoreKey dsK = ve.getAnnotation(DatastoreKey.class);
                    TypeMirror fieldType = ve.asType();
                    String fieldTypeClassName = fieldType.toString();

                    boolean good;

                    if (dsK.keyType() == KeyType.LONG) {
                        good = Long.class.getName().equals(fieldTypeClassName) || long.class.getName().equals(fieldTypeClassName);
                    } else {
                        good = String.class.getName().equals(fieldTypeClassName);
                    }
                    if (good) {
                        return "";
                    } else {
                        return String.format("Field %s's keyType does not match the type in '%s'", ve.getSimpleName().toString(), ((TypeElement) ve.getEnclosingElement()).getQualifiedName().toString());
                    }
                })
                .filter(m -> !m.isEmpty())
                .toList();

        messages.forEach(
                m -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, m)
        );
    }

    /**
     * Validates that the number of fields annotated with {@link DatastoreKey} in a class is at most 1
     *
     * @param annotation the annotation interface being processed
     * @param roundEnv environment for information about the current and prior round
     *
     * @since 1.0
     */
    private void validateKeyNumber(TypeElement annotation, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
        Set<VariableElement> fields = ElementFilter.fieldsIn(elements);

        Map<Element, List<VariableElement>> fieldsPerClass = fields.stream()
                .collect(Collectors.groupingBy(VariableElement::getEnclosingElement));

        List<String> messages = fieldsPerClass.keySet().stream()
                .map(e -> {
                    List<VariableElement> classFields = fieldsPerClass.get(e);
                    long numKeys = classFields.stream()
                            .filter(ve -> ve.getAnnotation(DatastoreKey.class) != null)
                            .count();

                    if (numKeys > 1) {
                        return String.format("Can only have 1 DatastoreKey in '%s'", ((TypeElement) e).getQualifiedName());
                    }

                    return "";
                })
                .filter(m -> !m.isEmpty())
                .toList();

        messages.forEach(
                m -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, m)
        );
    }
}
