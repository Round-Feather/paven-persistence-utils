package com.roundfeather.persistence.datastore.annotation.processor;

import com.google.auto.service.AutoService;
import com.roundfeather.persistence.datastore.annotation.DatastoreAncestor;
import com.roundfeather.persistence.datastore.annotation.KeyType;

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
import java.util.stream.IntStream;

/**
 * {@link AbstractProcessor} for processing the {@link DatastoreAncestor} annotation. Handles compile time validations
 * for classes and fields containing this annotation.
 *
 * <p>
 *     Validates that the orders of the {@link DatastoreAncestor} is not duplicated and that they are consecutive starting
 *     at 1
 * </p>
 * <p>
 *     Validates that the {@link KeyType} of the {@link DatastoreAncestor} matches the datatype of the annotated field
 * </p>
 *
 * @since 1.0
 */
@SupportedAnnotationTypes({"com.roundfeather.persistence.datastore.annotation.DatastoreAncestor"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class DatastoreAncestorProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            validateOrder(annotation, roundEnv);
            validateKeyType(annotation, roundEnv);
        }

        return true;
    }

    /**
     * Validates that the class with fields annotated with {@link DatastoreAncestor} all have a unique order and that
     * they are consecutive starting at 1
     *
     * @param annotation the annotation interface being processed
     * @param roundEnv environment for information about the current and prior round
     *
     * @since 1.0
     */
    private void validateOrder(TypeElement annotation, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
        Set<VariableElement> fields = ElementFilter.fieldsIn(elements);

        Map<Element, List<VariableElement>> fieldsPerClass = fields.stream()
                .collect(Collectors.groupingBy(VariableElement::getEnclosingElement));

        List<String> messages = fieldsPerClass.keySet().stream()
                .map(e -> {
                    List<VariableElement> classFields = fieldsPerClass.get(e);
                    Set<Integer> fieldOrders = classFields.stream()
                            .map(ve -> {
                                DatastoreAncestor dsA = ve.getAnnotation(DatastoreAncestor.class);
                                return dsA.order();
                            })
                            .collect(Collectors.toSet());

                    if (classFields.size() != fieldOrders.size()) {
                        return String.format("Duplicate ancestor orders in '%s'", ((TypeElement) e).getQualifiedName());
                    }

                    if (!fieldOrders.containsAll(IntStream.rangeClosed(1, classFields.size()).boxed().toList())) {
                        return String.format("Order of ancestors is not consecutive in '%s'", ((TypeElement) e).getQualifiedName());
                    }

                    return "";
                })
                .filter(m -> !m.isEmpty())
                .toList();

        messages.forEach(
                m -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, m)
        );
    }

    /**
     * Validates that the fields annotated with {@link DatastoreAncestor} have a {@link KeyType} that matches the type of
     * the field
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
                    DatastoreAncestor dsA = ve.getAnnotation(DatastoreAncestor.class);
                    TypeMirror fieldType = ve.asType();
                    String fieldTypeClassName = fieldType.toString();

                    boolean good;

                    if (dsA.keyType() == KeyType.LONG) {
                        good = Long.class.getName().equals(fieldTypeClassName) || long.class.getName().equals(fieldTypeClassName);
                    } else {
                        good = String.class.getName().equals(fieldTypeClassName);
                    }
                    if (good) {
                        return "";
                    } else {
                        return String.format("Field %s's ancestor keyType does not match the type in '%s'", ve.getSimpleName().toString(), ((TypeElement) ve.getEnclosingElement()).getQualifiedName().toString());
                    }
                })
                .filter(m -> !m.isEmpty())
                .toList();

        messages.forEach(
                m -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, m)
        );
    }
}
