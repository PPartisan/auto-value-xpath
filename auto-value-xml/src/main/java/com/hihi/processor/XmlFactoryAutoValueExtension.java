package com.hihi.processor;

import com.google.auto.service.AutoService;
import com.google.auto.value.extension.AutoValueExtension;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static com.gabrielittner.auto.value.util.AutoValueUtil.getFinalClassClassName;
import static com.gabrielittner.auto.value.util.AutoValueUtil.newFinalClassConstructorCall;
import static com.gabrielittner.auto.value.util.AutoValueUtil.newTypeSpecBuilder;
import static com.gabrielittner.auto.value.util.ElementUtil.getMatchingStaticMethod;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

@AutoService(AutoValueExtension.class)
public class XmlFactoryAutoValueExtension extends AutoValueExtension {

    private static final ClassName CN_STRING = ClassName.get(String.class);
    private static final ClassName CN_NODE = ClassName.get(Node.class);
    private static final ClassName CN_XPATH = ClassName.get(XPath.class);
    private static final ClassName CN_DOCUMENT = ClassName.get(Document.class);
    private static final ClassName CN_XPATH_EXPRESSION_EXCEPTION = ClassName.get(XPathExpressionException.class);
    private static final ClassName CN_RUNTIME_EXCEPTION = ClassName.get(RuntimeException.class);
    private static final ClassName CN_CHARSET = ClassName.get(Charset.class);
    private static final ClassName CN_INPUT_STREAM = ClassName.get(InputStream.class);
    private static final ClassName CN_BYTE_ARRAY_INPUT_STREAM = ClassName.get(ByteArrayInputStream.class);
    private static final ClassName CN_DOCUMENT_BUILDER_FACTORY = ClassName.get(DocumentBuilderFactory.class);
    private static final ClassName CN_DOCUMENT_BUILDER = ClassName.get(DocumentBuilder.class);
    private static final ClassName CN_XPATH_FACTORY = ClassName.get(XPathFactory.class);

    private static final ArrayTypeName CN_BYTE_ARRAY = ArrayTypeName.of(byte.class);

    private static final String FROM_XML_METHOD_NAME = "fromXml";
    private static final String FROM_XML_METHOD_NAME_GENERATED = "createFromXml";
    private static final String FROM_XML_PARAMETER_NAME = "xml";

    private static final String GET_NODE_FOR_XPATH_METHOD_NAME = "getNodeForXPath";
    private static final String GET_NODE_FOR_XPATH_PARAM_PATH = "path";
    private static final String GET_NODE_FOR_XPATH_PARAM_EXPRESSION = "expression";
    private static final String GET_NODE_FOR_XPATH_PARAM_DOCUMENT = "document";

    @Override
    public IncrementalExtensionType incrementalType(ProcessingEnvironment processingEnvironment) {
        //todo Explore possibility of different incremental types to improve performance
        //See "https://docs.gradle.org/current/userguide/java_plugin.html#sec:incremental_annotation_processing"
        return IncrementalExtensionType.ISOLATING;
    }

    //Only do anything if a static method exists that: (1) takes in a String argument, and (2) has the name "fromXml(String s)"
    @Override
    public boolean applicable(Context context) {
        final TypeElement valueClass = context.autoValueClass();
        final Optional<ExecutableElement> element =
                getMatchingStaticMethod(valueClass, ClassName.get(valueClass), CN_STRING);
        if(!element.isPresent()) {
            return false;
        }
        return element.get().getSimpleName().contentEquals(FROM_XML_METHOD_NAME);
    }

    @Override
    public String generateClass(Context context, String className, String classtoExtend, boolean isFinal) {

        final ImmutableList<ValueProperty> properties = ValueProperty.from(context);
        final TypeSpec.Builder subclass =
                newTypeSpecBuilder(context, className, classtoExtend, isFinal)
                        .addMethod(createFromXmlMethod(context, properties))
                        .addMethod(getNodeValueForXPath());

        return JavaFile.builder(context.packageName(), subclass.build())
                .indent("\t")
                .addStaticImport(XPathConstants.class, "NODE")
                .build()
                .toString();
    }

    private MethodSpec getNodeValueForXPath() {
        final MethodSpec.Builder getNodeValueForXPath =
                MethodSpec.methodBuilder(GET_NODE_FOR_XPATH_METHOD_NAME)
                        .addModifiers(PRIVATE, STATIC)
                        .addParameter(CN_XPATH, GET_NODE_FOR_XPATH_PARAM_PATH)
                        .addParameter(CN_STRING, GET_NODE_FOR_XPATH_PARAM_EXPRESSION)
                        .addParameter(CN_DOCUMENT, GET_NODE_FOR_XPATH_PARAM_DOCUMENT)
                        .returns(CN_NODE);

        getNodeValueForXPath.beginControlFlow("try");
        getNodeValueForXPath.addStatement(
                "return ($T) $L.evaluate($L, $L, NODE)",
                CN_NODE,
                GET_NODE_FOR_XPATH_PARAM_PATH,
                GET_NODE_FOR_XPATH_PARAM_EXPRESSION,
                GET_NODE_FOR_XPATH_PARAM_DOCUMENT
        );

        getNodeValueForXPath.nextControlFlow("catch ($T e)", CN_XPATH_EXPRESSION_EXCEPTION);

        getNodeValueForXPath.addStatement("final $T msg = $T.format(\"Could not evaluate path '%s'\", $L)",
                CN_STRING,
                CN_STRING,
                GET_NODE_FOR_XPATH_PARAM_EXPRESSION
        );

        getNodeValueForXPath.addStatement("throw new $T($L,$L)", CN_RUNTIME_EXCEPTION, "msg","e");
        getNodeValueForXPath.endControlFlow();

        return getNodeValueForXPath.build();
    }

    private MethodSpec createFromXmlMethod(Context context, ImmutableList<ValueProperty> properties) {
        final MethodSpec.Builder createFromXmlMethod = MethodSpec.methodBuilder(FROM_XML_METHOD_NAME_GENERATED)
                .addModifiers(STATIC)
                .returns(getFinalClassClassName(context))
                .addParameter(CN_STRING, FROM_XML_PARAMETER_NAME);

        createFromXmlMethod.addStatement("final $T utf8 = $T.forName($S)", CN_CHARSET, CN_CHARSET, "UTF-8");

        createFromXmlMethod.addStatement("final $T bytes = $L.getBytes($L)", CN_BYTE_ARRAY, FROM_XML_PARAMETER_NAME, "utf8");

        createFromXmlMethod.addCode("\n \n");

        createFromXmlMethod.addStatement("$T is = null", CN_INPUT_STREAM);

        createFromXmlMethod.beginControlFlow("try");

        createFromXmlMethod.addStatement("is = new $T($L)", CN_BYTE_ARRAY_INPUT_STREAM, "bytes");

        createFromXmlMethod.addCode("\n \n");

        createFromXmlMethod.addStatement("final $1T factory = $1T.newInstance()", CN_DOCUMENT_BUILDER_FACTORY);
        createFromXmlMethod.addStatement("final $T builder = factory.newDocumentBuilder()", CN_DOCUMENT_BUILDER);
        createFromXmlMethod.addStatement("final $T document = builder.parse($L)", CN_DOCUMENT, "is");

        createFromXmlMethod.addCode("\n \n");

        createFromXmlMethod.addStatement("final $T path = $T.newInstance().newXPath()", CN_XPATH, CN_XPATH_FACTORY);

        createFromXmlMethod.addCode("\n \n");

        final String[] names = new String[properties.size()];
        for(int i = 0; i < properties.size(); i++) {
            final ValueProperty property = properties.get(i);
            names[i] = property.humanName();

            if(property.isSupportedType()) {
                createFromXmlMethod.addStatement("final $T node$L = $L($L, $S, $L)",
                        CN_NODE,
                        i,
                        GET_NODE_FOR_XPATH_METHOD_NAME,
                        "path",
                        property.valueTagName(),
                        "document");
                createFromXmlMethod.addStatement(property.extractTagValueMethod(i));

                createFromXmlMethod.addCode("\n \n");

            } else {
                final String msg = String.format(
                        "Can not read type '%s' for property '%s'",
                        property.type(),
                        property.humanName()
                );
                throw new UnsupportedOperationException(msg);
            }
        }

        createFromXmlMethod.addCode("return ").addCode(newFinalClassConstructorCall(context, names));

        createFromXmlMethod.nextControlFlow(
                "catch ($T | $T | $T e)",
                IOException.class,
                ParserConfigurationException.class,
                SAXException.class
        );
        createFromXmlMethod.addStatement("throw new $T(e)", RuntimeException.class);
        createFromXmlMethod.nextControlFlow("finally");
        createFromXmlMethod.beginControlFlow("if (is != null)");
        createFromXmlMethod.beginControlFlow("try");
        createFromXmlMethod.addStatement("is.close()");
        createFromXmlMethod.nextControlFlow("catch ($T e)", Exception.class);
        createFromXmlMethod.addComment("Ignore");
        createFromXmlMethod.endControlFlow();

        createFromXmlMethod.endControlFlow();

        createFromXmlMethod.endControlFlow();

        return createFromXmlMethod.build();
    }

}
