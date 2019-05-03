package com.hihi.xmlprocessorapplication;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import static org.junit.Assert.*;

public class XmlPathAutoValueExtensionTest {

    private TestValue value;

    @Before
    public void setUp() {
        value = TestValue.fromXml(TEST_XML);
    }

    @Test(expected = NullPointerException.class)
    public void givenNullXmlString_whenCreatingFromXml_thenThrowNullPointerException() {
        TestValue.fromXml(null);
    }

    @Test
    public void givenInvalidXmlString_whenCreatingFromXml_thenThrowParseException() {
        try{
            TestValue.fromXml("invalid_xml");
        } catch (RuntimeException e) {
            assertEquals(SAXParseException.class, e.getCause().getClass());
        }
    }

    @Test
    public void givenXmlPathPointsToNonPrimitiveAttribute_whenCreatingFromXml_thenFieldContainsAttributeValue() {
        assertEquals("test_value_id", value.aStringAttribute());
    }

    @Test
    public void givenXmlPathPointsToPrimitiveAttribute_whenCreatingFromXml_thenFieldContainsAttributeValue() {
        assertEquals(5, value.aIntegerAttribute());
    }

    @Test
    public void givenXmlPathPointsToStringValue_whenCreatingFromXml_thenFieldContainsValue() {
        assertEquals("a_string", value.aString());
    }

    @Test
    public void givenXmlPathPointsToBooleanValue_whenCreatingFromXml_thenFieldContainsValue() {
        assertTrue(value.aBoolean());
    }

    @Test
    public void givenXmlPathPointsToDoubleValue_whenCreatingFromXml_thenFieldContainsValue() {
        assertEquals(987654321.123456D, value.aDouble(), 0);
    }

    @Test
    public void givenXmlPathPointsToFloatValue_whenCreatingFromXml_thenFieldContainsValue() {
        assertEquals(444.555F, value.aFloat(), 0F);
    }

    @Test
    public void givenXmlPathPointsToIntValue_whenCreatingFromXml_thenFieldContainsValue() {
        assertEquals(777892, value.anInt());
    }

    @Test
    public void givenXmlPathPointsToLongValue_whenCreatingFromXml_thenFieldContainsValue() {
        assertEquals(7234987235016749342L, value.aLong());
    }

    @Test
    public void givenXmlPathPointsToShortValue_whenCreatingFromXml_thenFieldContainsValue() {
        assertEquals(20, value.aShort());
    }

    @Test
    public void givenXmlPathPointsToNonExistentBoolean_andFieldIsNullable_whenCreatingFromXml_thenFieldIsNull() {
        assertNull(value.aNullableBoolean());
    }

    @Test
    public void givenXmlPathPointsToNonExistentNumber_andFieldIsNullable_whenCreatingFromXml_thenFieldIsNull() {
        assertNull(value.aNullableNumber());
    }

    @Test
    public void givenXmlPathPointsToNonExistentString_andFieldIsNullable_whenCreatingFromXml_thenFieldIsNull() {
        assertNull(value.aNullableString());
    }

    private static final String TEST_XML =
            "<?xml version=\"1.0\"?>\n" +
            "<testvalue>\n" +
            "   <item id=\"test_value_id\">\n" +
            "       <astring>a_string</astring>\n" +
            "       <aboolean>true</aboolean>\n" +
            "       <numbers count=\"5\">\n" +
            "           <adouble>987654321.123456</adouble>\n" +
            "           <afloat>444.555</afloat>\n" +
            "           <anint>777892</anint>\n" +
            "           <along>7234987235016749342</along>\n" +
            "           <ashort>20</ashort>\n" +
            "       </numbers>\n" +
            "   </item>\n" +
            "</testvalue>";
}