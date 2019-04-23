package com.hihi.xmlprocessorapplication;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.hihi.xml.XmlPath;

@AutoValue
abstract class TestValue {

    @XmlPath("/testvalue/item/@id")
    abstract String aStringAttribute();

    @XmlPath("/testvalue/item/numbers/@count")
    abstract int aIntegerAttribute();

    @XmlPath("/testvalue/item/astring/text()")
    abstract String aString();
    @XmlPath("/testvalue/item/aboolean/text()")
    abstract boolean aBoolean();
    @XmlPath("/testvalue/item/numbers/adouble/text()")
    abstract double aDouble();
    @XmlPath("/testvalue/item/numbers/afloat/text()")
    abstract float aFloat();
    @XmlPath("/testvalue/item/numbers/anint/text()")
    abstract int anInt();
    @XmlPath("/testvalue/item/numbers/along/text()")
    abstract long aLong();
    @XmlPath("/testvalue/item/numbers/ashort/text()")
    abstract short aShort();

    @XmlPath("/testvalue/item/no_boolean_here")
    @Nullable
    abstract Boolean aNullableBoolean();

    @XmlPath("/testvalue/item/no_float_here")
    @Nullable
    abstract Float aNullableNumber();


    @XmlPath("/testvalue/item/no_string_here")
    @Nullable
    abstract String aNullableString();

    static TestValue fromXml(String xml) {
        return AutoValue_TestValue.createFromXml(xml);
    }
}
