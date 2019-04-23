package com.hihi.processor;

import com.gabrielittner.auto.value.util.ElementUtil;
import com.gabrielittner.auto.value.util.Property;
import com.google.auto.value.extension.AutoValueExtension;
import com.google.common.collect.ImmutableList;
import com.hihi.xml.XmlPath;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.DOUBLE;
import static com.squareup.javapoet.TypeName.FLOAT;
import static com.squareup.javapoet.TypeName.INT;
import static com.squareup.javapoet.TypeName.LONG;
import static com.squareup.javapoet.TypeName.SHORT;

final class ValueProperty extends Property {

    static ImmutableList<ValueProperty> from(AutoValueExtension.Context context) {
        final ImmutableList.Builder<ValueProperty> values = ImmutableList.builder();
        for(Map.Entry<String, ExecutableElement> entry : context.properties().entrySet()) {
            values.add(new ValueProperty(entry.getKey(), entry.getValue()));
        }
        return values.build();
    }

    private static final List<TypeName> SUPPORTED_TYPES =
            Arrays.asList(
                    TypeName.get(String.class),
                    DOUBLE,
                    DOUBLE.box(),
                    FLOAT,
                    FLOAT.box(),
                    INT,
                    INT.box(),
                    LONG,
                    LONG.box(),
                    SHORT,
                    SHORT.box(),
                    BOOLEAN,
                    BOOLEAN.box()
            );

    private static final ClassName CN_INTEGER = ClassName.get(Integer.class);
    private static final ClassName CN_BOOLEAN = ClassName.get(Boolean.class);
    private static final ClassName CN_DOUBLE = ClassName.get(Double.class);
    private static final ClassName CN_FLOAT = ClassName.get(Float.class);
    private static final ClassName CN_LONG = ClassName.get(Long.class);
    private static final ClassName CN_SHORT = ClassName.get(Short.class);

    private final String valueTagName;
    private final boolean isSupportedType;

    private ValueProperty(String humanName, ExecutableElement element) {
        super(humanName, element);
        valueTagName = (String) ElementUtil.getAnnotationValue(element, XmlPath.class, "value");
        isSupportedType = SUPPORTED_TYPES.contains(type());
    }

    boolean isSupportedType() {
        return isSupportedType;
    }

    String valueTagName() {
        return valueTagName == null ? humanName() : valueTagName;
    }

    CodeBlock extractTagValueMethod(int index) {
        if(!isSupportedType) return null;

        if(typeIs(BOOLEAN)) {
            return primitiveNode(CN_BOOLEAN, index);
        } else if(typeIs(DOUBLE)) {
            return primitiveNode(CN_DOUBLE, index);
        } else if(typeIs(FLOAT)) {
            return primitiveNode(CN_FLOAT, index);
        } else if(typeIs(INT)) {
            return CodeBlock.of("final $1T $2N = $3T.parseInt(node$4L.getNodeValue())", type(), humanName(), CN_INTEGER, index);
        } else if(typeIs(LONG)) {
            return primitiveNode(CN_LONG, index);
        } else if(typeIs(SHORT)) {
            return primitiveNode(CN_SHORT, index);
        } else if (typeIs(INT.box())) {
            if(nullable()) {
                return CodeBlock.of("final $1T $2N = node$3L == null ? null : $1T.parseInt(node$3L.getNodeValue())", type(), humanName(), index);
            } else {
                return CodeBlock.of("final $1T $2N = $1T.parseInt(node$3L.getNodeValue())", type(), humanName(), index);
            }
        } else if (typeIs(BOOLEAN.box())) {
            return boxedNode(CN_BOOLEAN, index);
        } else if (typeIs(DOUBLE.box())) {
            return boxedNode(CN_DOUBLE, index);
        } else if (typeIs(FLOAT.box())) {
            return boxedNode(CN_FLOAT, index);
        } else if (typeIs(LONG.box())) {
            return boxedNode(CN_LONG, index);
        } else if (typeIs(SHORT.box())) {
            return boxedNode(CN_SHORT, index);
        } else if(typeIs(TypeName.get(String.class))) {
            if (nullable()) {
                return CodeBlock.of("final $1T $2N = node$3L == null ? null : node$3L.getNodeValue()", type(), humanName(), index);
            } else {
                return CodeBlock.of("final $T $N = node$L.getNodeValue()", type(), humanName(), index);
            }
        }

        throw new AssertionError(String.format("supportedType is true but type '%s' isn't handled", type()));
    }

    private CodeBlock nullableBoxedNode(ClassName cn, int index) {
        return CodeBlock.of("final $1T $2N = node$4L == null ? null : $3T.parse$3T(node$4L.getNodeValue())", type(), humanName(), cn, index);
    }

    private CodeBlock primitiveNode(ClassName cn, int index) {
        return CodeBlock.of("final $1T $2N = $3T.parse$3T(node$4L.getNodeValue())", type(), humanName(), cn, index);
    }

    private CodeBlock boxedNode(ClassName cn, int index) {
        if(nullable()) {
            return nullableBoxedNode(cn, index);
        } else {
            return primitiveNode(cn, index);
        }
    }

    private boolean typeIs(TypeName tn) {
        return type().equals(tn);
    }

}
