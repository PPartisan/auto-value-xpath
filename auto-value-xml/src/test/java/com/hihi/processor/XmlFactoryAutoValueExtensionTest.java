package com.hihi.processor;

import com.google.auto.value.processor.AutoValueProcessor;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;

public class XmlFactoryAutoValueExtensionTest {

    @Test
    public void simple() {
        final JavaFileObject source = JavaFileObjects.forSourceString("test.Test", "" +
                "package test;\n\n" +
                "import com.google.auto.value.AutoValue;\n" +
                "import com.hihi.xml.XmlPath;\n" +
                "\n" +
                "@AutoValue\n" +
                "abstract class Test {\n" +
                "static Test fromXml(String xml) {\n" +
                "       return null;\n" +
                "   }\n" +
                "\n" +
                "   @XmlPath(\"/path/to/string/text()\")\n" +
                "   abstract String content();\n" +
                "\n" +
                "   @XmlPath(\"/path/to/attribute/@code\")\n" +
                "   abstract int code();\n" +
                "}");

        final JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Test",
                "package test;\n" +
                        "\n" +
                        "import static javax.xml.xpath.XPathConstants.NODE;\n" +
                        "\n" +
                        "import java.io.ByteArrayInputStream;\n" +
                        "import java.io.IOException;\n" +
                        "import java.io.InputStream;\n" +
                        "import java.lang.Exception;\n" +
                        "import java.lang.Integer;\n" +
                        "import java.lang.RuntimeException;\n" +
                        "import java.lang.String;\n" +
                        "import java.nio.charset.Charset;\n" +
                        "import javax.xml.parsers.DocumentBuilder;\n" +
                        "import javax.xml.parsers.DocumentBuilderFactory;\n" +
                        "import javax.xml.parsers.ParserConfigurationException;\n" +
                        "import javax.xml.xpath.XPath;\n" +
                        "import javax.xml.xpath.XPathExpressionException;\n" +
                        "import javax.xml.xpath.XPathFactory;\n" +
                        "import org.w3c.dom.Document;\n" +
                        "import org.w3c.dom.Node;\n" +
                        "import org.xml.sax.SAXException;\n" +
                        "\n" +
                        "final class AutoValue_Test extends $AutoValue_Test {\n" +
                        "   AutoValue_Test(String content, int code) {\n" +
                        "       super(content, code);\n" +
                        "   }\n" +
                        "\n" +
                        "   static AutoValue_Test createFromXml(String xml) {\n" +
                        "       final Charset utf8 = Charset.forName(\"UTF-8\");\n" +
                        "       final byte[] bytes = xml.getBytes(utf8);\n" +
                        "\n" +
                        "       InputStream is = null;\n" +
                        "       try {\n" +
                        "           is = new ByteArrayInputStream(bytes);\n" +
                        "\n" +
                        "           final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();\n" +
                        "           final DocumentBuilder builder = factory.newDocumentBuilder();\n" +
                        "           final Document document = builder.parse(is);\n" +
                        "           \n" +
                        "           final XPath path = XPathFactory.newInstance().newXPath();\n" +
                        "           \n" +
                        "           final Node node0 = getNodeForXPath(path, \"/path/to/string/text()\", document);\n" +
                        "           final String content = node0.getNodeValue();\n" +
                        "           \n" +
                        "           final Node node1 = getNodeForXPath(path, \"/path/to/attribute/@code\", document);\n" +
                        "           final int code = Integer.parseInt(node1.getNodeValue());\n" +
                        "\n" +
                        "           return new AutoValue_Test(content, code);\n" +
                        "       } catch (IOException | ParserConfigurationException | SAXException e) {\n" +
                        "           throw new RuntimeException(e);\n" +
                        "       } finally {\n" +
                        "           if (is != null) {\n" +
                        "               try {\n" +
                        "                   is.close();\n" +
                        "               } catch (Exception e) {\n" +
                        "                   // Ignore\n" +
                        "               }\n" +
                        "           }\n" +
                        "       }\n" +
                        "   }\n" +
                        "\n" +
                        "   private static Node getNodeForXPath(XPath path, String expression, Document document) {\n" +
                        "       try {\n" +
                        "           return (Node) path.evaluate(expression, document, NODE);\n" +
                        "       } catch (XPathExpressionException e) {\n" +
                        "           final String msg = String.format(\"Could not evaluate path '%s'\", expression);\n" +
                        "           throw new RuntimeException(msg,e);\n" +
                        "       }\n" +
                        "   }\n" +
                        "}");

        assertAbout(javaSources())
                .that(singletonList(source))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void unsupported() {
        final JavaFileObject source = JavaFileObjects.forSourceString("test.Test", "" +
                "package test;\n\n" +
                "import com.google.auto.value.AutoValue;\n" +
                "import com.hihi.xml.XmlPath;\n" +
                "\n" +
                "@AutoValue\n" +
                "abstract class Test {\n" +
                "static Test fromXml(String xml) {\n" +
                "       return null;\n" +
                "   }\n" +
                "\n" +
                "   @XmlPath(\"/path/to/string/text()\")\n" +
                "   abstract StringBuilder content();\n" +
                "\n" +
                "   @XmlPath(\"/path/to/attribute/@code\")\n" +
                "   abstract int code();\n" +
                "}");

        try {
            assertAbout(javaSources())
                    .that(singletonList(source))
                    .processedWith(new AutoValueProcessor())
                    .failsToCompile();
        } catch (RuntimeException e) {
            assertThat(e).hasCauseThat().isInstanceOf(UnsupportedOperationException.class);
            assertThat(e).hasCauseThat().hasMessage("Can not read type 'java.lang.StringBuilder' for property 'content'");
        }
    }

    @Test
    public void allPrimitiveTypes() {
        final JavaFileObject source = JavaFileObjects.forSourceString("test.Test", "" +
                "package test;\n\n" +
                "import com.google.auto.value.AutoValue;\n" +
                "import com.hihi.xml.XmlPath;\n" +
                "\n" +
                "@AutoValue\n" +
                "abstract class Test {\n" +
                "\n" +
                "    @XmlPath(\"/path/to/bool/text()\")\n" +
                "    abstract boolean aBoolean();\n" +
                "\n" +
                "    @XmlPath(\"/path/to/double/text()\")\n" +
                "    abstract double aDouble();\n" +
                "\n" +
                "    @XmlPath(\"/path/to/float/text()\")\n" +
                "    abstract float aFloat();\n" +
                "\n" +
                "    @XmlPath(\"/path/to/int/text()\")\n" +
                "    abstract int aInt();\n" +
                "\n" +
                "    @XmlPath(\"/path/to/long/text()\")\n" +
                "    abstract long aLong();\n" +
                "\n" +
                "    @XmlPath(\"/path/to/short/text()\")\n" +
                "    abstract short aShort();\n" +
                "\n" +
                "    static Test fromXml(String xml) {\n" +
                "        return AutoValue_Test.fromXml(xml);\n" +
                "    }\n" +
                "\n" +
                "}");

        final JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Test",
                "package test;\n\n" +
                        "import static javax.xml.xpath.XPathConstants.NODE;\n" +
                        "\n" +
                        "import java.io.ByteArrayInputStream;\n" +
                        "import java.io.IOException;\n" +
                        "import java.io.InputStream;\n" +
                        "import java.lang.Boolean;\n" +
                        "import java.lang.Double;\n" +
                        "import java.lang.Exception;\n" +
                        "import java.lang.Float;\n" +
                        "import java.lang.Integer;\n" +
                        "import java.lang.Long;\n" +
                        "import java.lang.RuntimeException;\n" +
                        "import java.lang.Short;\n" +
                        "import java.lang.String;\n" +
                        "import java.nio.charset.Charset;\n" +
                        "import javax.xml.parsers.DocumentBuilder;\n" +
                        "import javax.xml.parsers.DocumentBuilderFactory;\n" +
                        "import javax.xml.parsers.ParserConfigurationException;\n" +
                        "import javax.xml.xpath.XPath;\n" +
                        "import javax.xml.xpath.XPathExpressionException;\n" +
                        "import javax.xml.xpath.XPathFactory;\n" +
                        "import org.w3c.dom.Document;\n" +
                        "import org.w3c.dom.Node;\n" +
                        "import org.xml.sax.SAXException;\n" +
                        "\n" +
                        "final class AutoValue_Test extends $AutoValue_Test {\n" +
                        "  AutoValue_Test(boolean aBoolean, double aDouble, float aFloat, int aInt, long aLong,\n" +
                        "      short aShort) {\n" +
                        "    super(aBoolean, aDouble, aFloat, aInt, aLong, aShort);\n" +
                        "  }\n" +
                        "\n" +
                        "  static AutoValue_Test createFromXml(String xml) {\n" +
                        "    final Charset utf8 = Charset.forName(\"UTF-8\");\n" +
                        "    final byte[] bytes = xml.getBytes(utf8);\n" +
                        "    \n" +
                        "    InputStream is = null;\n" +
                        "    try {\n" +
                        "      is = new ByteArrayInputStream(bytes);\n" +
                        "      \n" +
                        "      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();\n" +
                        "      final DocumentBuilder builder = factory.newDocumentBuilder();\n" +
                        "      final Document document = builder.parse(is);\n" +
                        "      \n" +
                        "      final XPath path = XPathFactory.newInstance().newXPath();\n" +
                        "      \n" +
                        "      final Node node0 = getNodeForXPath(path, \"/path/to/bool/text()\", document);\n" +
                        "      final boolean aBoolean = Boolean.parseBoolean(node0.getNodeValue());\n" +
                        "      \n" +
                        "      final Node node1 = getNodeForXPath(path, \"/path/to/double/text()\", document);\n" +
                        "      final double aDouble = Double.parseDouble(node1.getNodeValue());\n" +
                        "      \n" +
                        "      final Node node2 = getNodeForXPath(path, \"/path/to/float/text()\", document);\n" +
                        "      final float aFloat = Float.parseFloat(node2.getNodeValue());\n" +
                        "      \n" +
                        "      final Node node3 = getNodeForXPath(path, \"/path/to/int/text()\", document);\n" +
                        "      final int aInt = Integer.parseInt(node3.getNodeValue());\n" +
                        "      \n" +
                        "      final Node node4 = getNodeForXPath(path, \"/path/to/long/text()\", document);\n" +
                        "      final long aLong = Long.parseLong(node4.getNodeValue());\n" +
                        "      \n" +
                        "      final Node node5 = getNodeForXPath(path, \"/path/to/short/text()\", document);\n" +
                        "      final short aShort = Short.parseShort(node5.getNodeValue());\n" +
                        "      \n" +
                        "      return new AutoValue_Test(aBoolean, aDouble, aFloat, aInt, aLong, aShort);\n" +
                        "    } catch (IOException | ParserConfigurationException | SAXException e) {\n" +
                        "      throw new RuntimeException(e);\n" +
                        "    } finally {\n" +
                        "      if (is != null) {\n" +
                        "        try {\n" +
                        "          is.close();\n" +
                        "        } catch (Exception e) {\n" +
                        "          // Ignore\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "\n" +
                        "  private static Node getNodeForXPath(XPath path, String expression, Document document) {\n" +
                        "    try {\n" +
                        "      return (Node) path.evaluate(expression, document, NODE);\n" +
                        "    } catch (XPathExpressionException e) {\n" +
                        "      final String msg = String.format(\"Could not evaluate path '%s'\", expression);\n" +
                        "      throw new RuntimeException(msg,e);\n" +
                        "    }\n" +
                        "  }\n" +
                        "}");

        assertAbout(javaSources())
                .that(singletonList(source))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void allBoxedTypes() {
        final JavaFileObject source = JavaFileObjects.forSourceString("test.Test", "" +
                "package test;\n\n" +
                "import com.google.auto.value.AutoValue;\n" +
                "import com.hihi.xml.XmlPath;\n" +
                "\n" +
                "@AutoValue\n" +
                "abstract class Test {\n" +
                "\n" +
                "    @XmlPath(\"/path/to/bool/text()\")\n" +
                "    abstract Boolean aBoolean();\n" +
                "\n" +
                "    @XmlPath(\"/path/to/double/text()\")\n" +
                "    abstract Double aDouble();\n" +
                "\n" +
                "    @XmlPath(\"/path/to/float/text()\")\n" +
                "    abstract Float aFloat();\n" +
                "\n" +
                "    @XmlPath(\"/path/to/int/text()\")\n" +
                "    abstract Integer aInt();\n" +
                "\n" +
                "    @XmlPath(\"/path/to/long/text()\")\n" +
                "    abstract Long aLong();\n" +
                "\n" +
                "    @XmlPath(\"/path/to/short/text()\")\n" +
                "    abstract Short aShort();\n" +
                "\n" +
                "    static Test fromXml(String xml) {\n" +
                "        return AutoValue_Test.fromXml(xml);\n" +
                "    }\n" +
                "\n" +
                "}");

        final JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Test",
                "package test;\n\n" +
                        "import static javax.xml.xpath.XPathConstants.NODE;\n" +
                        "\n" +
                        "import java.io.ByteArrayInputStream;\n" +
                        "import java.io.IOException;\n" +
                        "import java.io.InputStream;\n" +
                        "import java.lang.Boolean;\n" +
                        "import java.lang.Double;\n" +
                        "import java.lang.Exception;\n" +
                        "import java.lang.Float;\n" +
                        "import java.lang.Integer;\n" +
                        "import java.lang.Long;\n" +
                        "import java.lang.RuntimeException;\n" +
                        "import java.lang.Short;\n" +
                        "import java.lang.String;\n" +
                        "import java.nio.charset.Charset;\n" +
                        "import javax.xml.parsers.DocumentBuilder;\n" +
                        "import javax.xml.parsers.DocumentBuilderFactory;\n" +
                        "import javax.xml.parsers.ParserConfigurationException;\n" +
                        "import javax.xml.xpath.XPath;\n" +
                        "import javax.xml.xpath.XPathExpressionException;\n" +
                        "import javax.xml.xpath.XPathFactory;\n" +
                        "import org.w3c.dom.Document;\n" +
                        "import org.w3c.dom.Node;\n" +
                        "import org.xml.sax.SAXException;" +
                        "\n\n" +
                        "final class AutoValue_Test extends $AutoValue_Test {\n" +
                        "  AutoValue_Test(Boolean aBoolean, Double aDouble, Float aFloat, Integer aInt, Long aLong,\n" +
                        "      Short aShort) {\n" +
                        "    super(aBoolean, aDouble, aFloat, aInt, aLong, aShort);\n" +
                        "  }\n" +
                        "\n" +
                        "  static AutoValue_Test createFromXml(String xml) {\n" +
                        "    final Charset utf8 = Charset.forName(\"UTF-8\");\n" +
                        "    final byte[] bytes = xml.getBytes(utf8);\n" +
                        "    \n" +
                        "    InputStream is = null;\n" +
                        "    try {\n" +
                        "      is = new ByteArrayInputStream(bytes);\n" +
                        "      \n" +
                        "      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();\n" +
                        "      final DocumentBuilder builder = factory.newDocumentBuilder();\n" +
                        "      final Document document = builder.parse(is);\n" +
                        "      \n" +
                        "      final XPath path = XPathFactory.newInstance().newXPath();\n" +
                        "      \n" +
                        "      final Node node0 = getNodeForXPath(path, \"/path/to/bool/text()\", document);\n" +
                        "      final Boolean aBoolean = Boolean.parseBoolean(node0.getNodeValue());\n" +
                        "      \n" +
                        "      final Node node1 = getNodeForXPath(path, \"/path/to/double/text()\", document);\n" +
                        "      final Double aDouble = Double.parseDouble(node1.getNodeValue());\n" +
                        "      \n" +
                        "      final Node node2 = getNodeForXPath(path, \"/path/to/float/text()\", document);\n" +
                        "      final Float aFloat = Float.parseFloat(node2.getNodeValue());\n" +
                        "      \n" +
                        "      final Node node3 = getNodeForXPath(path, \"/path/to/int/text()\", document);\n" +
                        "      final Integer aInt = Integer.parseInt(node3.getNodeValue());\n" +
                        "      \n" +
                        "      final Node node4 = getNodeForXPath(path, \"/path/to/long/text()\", document);\n" +
                        "      final Long aLong = Long.parseLong(node4.getNodeValue());\n" +
                        "      \n" +
                        "      final Node node5 = getNodeForXPath(path, \"/path/to/short/text()\", document);\n" +
                        "      final Short aShort = Short.parseShort(node5.getNodeValue());\n" +
                        "      \n" +
                        "      return new AutoValue_Test(aBoolean, aDouble, aFloat, aInt, aLong, aShort);\n" +
                        "    } catch (IOException | ParserConfigurationException | SAXException e) {\n" +
                        "      throw new RuntimeException(e);\n" +
                        "    } finally {\n" +
                        "      if (is != null) {\n" +
                        "        try {\n" +
                        "          is.close();\n" +
                        "        } catch (Exception e) {\n" +
                        "          // Ignore\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "\n" +
                        "  private static Node getNodeForXPath(XPath path, String expression, Document document) {\n" +
                        "    try {\n" +
                        "      return (Node) path.evaluate(expression, document, NODE);\n" +
                        "    } catch (XPathExpressionException e) {\n" +
                        "      final String msg = String.format(\"Could not evaluate path '%s'\", expression);\n" +
                        "      throw new RuntimeException(msg,e);\n" +
                        "    }\n" +
                        "  }\n" +
                        "}");

        assertAbout(javaSources())
                .that(singletonList(source))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void nullable() {
        final JavaFileObject source = JavaFileObjects.forSourceString("test.Test", "" +
                "package test;\n\n" +
                "import com.google.auto.value.AutoValue;\n" +
                "import com.hihi.xml.XmlPath;" +
                "import javax.annotation.Nullable;\n" +
                "\n" +
                "@AutoValue\n" +
                "abstract class Test {\n" +
                "static Test fromXml(String xml) {\n" +
                "       return null;\n" +
                "   }\n" +
                "\n" +
                "   @XmlPath(\"/path/to/string/text()\")\n" +
                "   @Nullable" +
                "   abstract String content();\n" +
                "}");

        final JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Test",
                "package test;\n" +
                        "\n" +
                        "import static javax.xml.xpath.XPathConstants.NODE;\n" +
                        "\n" +
                        "import java.io.ByteArrayInputStream;\n" +
                        "import java.io.IOException;\n" +
                        "import java.io.InputStream;\n" +
                        "import java.lang.Exception;\n" +
                        "import java.lang.RuntimeException;\n" +
                        "import java.lang.String;\n" +
                        "import java.nio.charset.Charset;\n" +
                        "import javax.xml.parsers.DocumentBuilder;\n" +
                        "import javax.xml.parsers.DocumentBuilderFactory;\n" +
                        "import javax.xml.parsers.ParserConfigurationException;\n" +
                        "import javax.xml.xpath.XPath;\n" +
                        "import javax.xml.xpath.XPathExpressionException;\n" +
                        "import javax.xml.xpath.XPathFactory;\n" +
                        "import org.w3c.dom.Document;\n" +
                        "import org.w3c.dom.Node;\n" +
                        "import org.xml.sax.SAXException;\n" +
                        "\n" +
                        "final class AutoValue_Test extends $AutoValue_Test {\n" +
                        "   AutoValue_Test(String content) {\n" +
                        "       super(content);\n" +
                        "   }\n" +
                        "\n" +
                        "   static AutoValue_Test createFromXml(String xml) {\n" +
                        "       final Charset utf8 = Charset.forName(\"UTF-8\");\n" +
                        "       final byte[] bytes = xml.getBytes(utf8);\n" +
                        "\n" +
                        "       InputStream is = null;\n" +
                        "       try {\n" +
                        "           is = new ByteArrayInputStream(bytes);\n" +
                        "\n" +
                        "           final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();\n" +
                        "           final DocumentBuilder builder = factory.newDocumentBuilder();\n" +
                        "           final Document document = builder.parse(is);\n" +
                        "           \n" +
                        "           final XPath path = XPathFactory.newInstance().newXPath();\n" +
                        "           \n" +
                        "           final Node node0 = getNodeForXPath(path, \"/path/to/string/text()\", document);\n" +
                        "           final String content = node0 == null ? null : node0.getNodeValue();\n" +
                        "\n" +
                        "           return new AutoValue_Test(content);\n" +
                        "       } catch (IOException | ParserConfigurationException | SAXException e) {\n" +
                        "           throw new RuntimeException(e);\n" +
                        "       } finally {\n" +
                        "           if (is != null) {\n" +
                        "               try {\n" +
                        "                   is.close();\n" +
                        "               } catch (Exception e) {\n" +
                        "                   // Ignore\n" +
                        "               }\n" +
                        "           }\n" +
                        "       }\n" +
                        "   }\n" +
                        "\n" +
                        "   private static Node getNodeForXPath(XPath path, String expression, Document document) {\n" +
                        "       try {\n" +
                        "           return (Node) path.evaluate(expression, document, NODE);\n" +
                        "       } catch (XPathExpressionException e) {\n" +
                        "           final String msg = String.format(\"Could not evaluate path '%s'\", expression);\n" +
                        "           throw new RuntimeException(msg,e);\n" +
                        "       }\n" +
                        "   }\n" +
                        "}");

        assertAbout(javaSources())
                .that(singletonList(source))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

}